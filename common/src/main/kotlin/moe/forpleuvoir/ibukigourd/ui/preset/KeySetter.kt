package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.input.*
import moe.forpleuvoir.ibukigourd.text.*
import moe.forpleuvoir.ibukigourd.ui.icon.EditNote
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.KeyboardAlt
import moe.forpleuvoir.ibukigourd.ui.icon.KeyboardArrowDown
import moe.forpleuvoir.ibukigourd.ui.util.toNebulaColor
import kotlin.time.Duration.Companion.milliseconds

private val ALL_KEYS by lazy {
    Keyboard.entries.map { it as KeyCode } + MouseButton.entries.map { it as KeyCode }
}

val Keybind.hoverText: MutableText
    get() {
        val conflictText = IGLang.keybindConflict
        val text = Literal(keys.joinToString(separator = " + ") {
            if (InputHandler.wasKeyPressed(Keyboard.LEFT_SHIFT)) {
                it.translationKey
            } else it.keyName
        })
        if (keys.count() == 0) text.append(IGLang.pressToSetting)
        var count = 0
        InputHandler.detectKeyConflicts(this).forEach {
            count++
            conflictText.appendNewLine().appendLiteral(" - ").append(it.name)
        }
        return if (count > 0) text.copy().appendNewLine().append(conflictText)
        else text
    }

@Composable
fun KeyCodeSetButton(
    value: KeyCode,
    onValueChange: (KeyCode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputting by remember { mutableStateOf(false) }

    LaunchedEffect(inputting) {
        if (!inputting) return@LaunchedEffect

        val captureKeybind = Keybind(
            *ALL_KEYS.toTypedArray(),
            defaultSetting = KeybindSetting(
                passthrough = false,
                strict = false,
                env = KeyEnvironment.Any,
            ),
        )
        captureKeybind.name = Literal("#${IbukiGourd.MOD_ID}.capture_keybind")
        val disposable = InputHandler.register(captureKeybind)
        try {
            while (isActive) {
                withFrameNanos {
                    val current = InputHandler.pressedKeys.toList().lastOrNull()
                    if (current != null) {
                        onValueChange(current)
                        inputting = false
                    }
                }
            }
        } finally {
            disposable.dispose()
        }
    }

    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier = modifier) {
        val tooltipModifier = when {
            maxWidth == Dp.Infinity -> Modifier
            else                    -> Modifier.fillMaxWidth()
        }
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = {
                var keyTooltip by remember {
                    mutableStateOf(
                        if (inputting) IGLang.releaseToSaveSetting
                        else if (InputHandler.wasKeyPressed(Keyboard.LEFT_SHIFT)) Texts.literal(value.translationKey) else value.keyNameText
                    )
                }
                LaunchedEffect(Unit) {
                    while (isActive) {
                        keyTooltip = if (inputting) IGLang.releaseToSaveSetting
                        else if (InputHandler.wasKeyPressed(Keyboard.LEFT_SHIFT)) Texts.literal(value.translationKey) else value.keyNameText
                        delay(16.milliseconds)
                    }
                }
                PlainTooltip {
                    Text(keyTooltip, textAlign = TextAlign.Center)
                }
            },
            state = tooltipState,
            modifier = tooltipModifier,
        ) {
            Button(
                onClick = {
                    if (!inputting) {
                        inputting = true
                        scope.launch {
                            tooltipState.show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.KeyboardAlt, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (inputting) IGLang.pressToSetting
                    else value.keyNameText
                )
            }
        }
    }
}


@Composable
fun KeybindSetButton(
    keybind: Keybind,
    onValueChange: (Keybind) -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputting by remember { mutableStateOf(false) }
    var displayKeys by remember { mutableStateOf<List<KeyCode>>(emptyList()) }

    LaunchedEffect(inputting) {
        if (!inputting) return@LaunchedEffect

        val captureKeybind = Keybind(
            *ALL_KEYS.toTypedArray(),
            defaultSetting = KeybindSetting(
                passthrough = false,
                strict = false,
                env = KeyEnvironment.Any
            )
        )
        captureKeybind.name = Literal("#${IbukiGourd.MOD_ID}.capture_keybind")
        val disposable = InputHandler.register(captureKeybind)
        try {
            val trackedKeys = mutableListOf<KeyCode>()
            while (isActive) {
                withFrameNanos {
                    val current = InputHandler.pressedKeys.toList()
                    if (current.isNotEmpty()) {
                        if (Keyboard.ESCAPE in current) {
                            keybind.setKey()
                            onValueChange(keybind)
                            inputting = false
                        } else {
                            current.forEach { if (it !in trackedKeys) trackedKeys.add(it) }
                            displayKeys = trackedKeys.toList()
                        }
                    } else if (trackedKeys.isNotEmpty()) {
                        keybind.setKey(*trackedKeys.toTypedArray())
                        onValueChange(keybind)
                        inputting = false
                    }
                }
            }
        } finally {
            disposable.dispose()
        }
    }

    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier = modifier) {
        val tooltipModifier = when {
            maxWidth == Dp.Infinity -> Modifier
            else                    -> Modifier.fillMaxWidth()
        }
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = {
                var keyTooltip by remember {
                    mutableStateOf(
                        if (inputting) IGLang.releaseToSaveSetting
                        else keybind.hoverText
                    )
                }
                LaunchedEffect(Unit) {
                    while (isActive) {
                        keyTooltip = if (inputting) {
                            val keys = displayKeys
                            if (keys.isEmpty()) IGLang.releaseToSaveSetting
                            else Literal(keys.joinToString(" + ") { it.keyName }).also {
                                it.appendNewLine().append(IGLang.releaseToSaveSetting)
                            }
                        } else keybind.hoverText
                        delay(16.milliseconds)
                    }
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.inverseSurface,
                ) {
                    Text(
                        keyTooltip,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        textAlign = TextAlign.Start,
                    )
                }
            },
            state = tooltipState,
            modifier = tooltipModifier,
        ) {
            Button(
                onClick = {
                    if (!inputting) {
                        displayKeys = emptyList()
                        inputting = true
                        scope.launch { tooltipState.show() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.KeyboardAlt, null)
                Spacer(Modifier.width(8.dp))
                //TODO 优化冲突显示
                if (InputHandler.detectKeyConflicts(keybind).count() > 0) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.KeyboardAlt, null)
                }
                Text(
                    if (inputting) {
                        val text = displayKeys.joinToString(" + ") { it.keyName }
                        if (text.isEmpty()) IGLang.pressToSetting
                        else Literal(text)
                    } else keybind.asText
                )
            }
        }
    }
}


@Composable
fun KeyBindSettingSetButton(
    keybindSetting: KeybindSetting,
    onValueChange: (KeybindSetting) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }
    var tempSetting by remember { mutableStateOf(keybindSetting) }

    IconButton(
        onClick = {
            tempSetting = keybindSetting
            showDialog = true
        },
        modifier = modifier,
    ) {
        Icon(Icons.EditNote, IGLang.edit.plainText)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Keybind Settings") },
            text = {
                KeybindSettingColumn(
                    keybindSetting = tempSetting,
                    onValueChange = { tempSetting = it },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(tempSetting)
                    showDialog = false
                }) { Text(IGLang.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(IGLang.cancel) }
            }
        )
    }
}

@Composable
fun KeybindSettingColumn(
    keybindSetting: KeybindSetting,
    onValueChange: (KeybindSetting) -> Unit,
    modifier: Modifier = Modifier,
) {
    var envExpanded by remember { mutableStateOf(false) }
    var triggerExpanded by remember { mutableStateOf(false) }
    var longPressThresholdText by remember { mutableStateOf(keybindSetting.longPressThreshold.toString()) }
    var repeatIntervalText by remember { mutableStateOf(keybindSetting.repeatInterval.toString()) }

    LaunchedEffect(keybindSetting.longPressThreshold) {
        longPressThresholdText = keybindSetting.longPressThreshold.toString()
    }
    LaunchedEffect(keybindSetting.repeatInterval) {
        repeatIntervalText = keybindSetting.repeatInterval.toString()
    }

    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Passthrough", modifier = Modifier.weight(1f))
            Switch(checked = keybindSetting.passthrough, onCheckedChange = {
                onValueChange(keybindSetting.copy(passthrough = it))
            })
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Strict", modifier = Modifier.weight(1f))
            Switch(checked = keybindSetting.strict, onCheckedChange = {
                onValueChange(keybindSetting.copy(strict = it))
            })
        }

        Spacer(Modifier.height(12.dp))

        Text("Environment")
        Spacer(Modifier.height(4.dp))
        Box {
            OutlinedButton(onClick = { envExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(keybindSetting.env.key, modifier = Modifier.weight(1f))
                Icon(Icons.KeyboardArrowDown, null)
            }
            DropdownMenu(expanded = envExpanded, onDismissRequest = { envExpanded = false }) {
                KeyEnvironment.entries.forEach { env ->
                    DropdownMenuItem(
                        text = { Text(env.key) },
                        onClick = {
                            onValueChange(keybindSetting.copy(env = env))
                            envExpanded = false
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text("Trigger")
        Spacer(Modifier.height(4.dp))
        Box {
            OutlinedButton(onClick = { triggerExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(keybindSetting.trigger.displayName.plainText, modifier = Modifier.weight(1f))
                Icon(Icons.KeyboardArrowDown, null)
            }
            DropdownMenu(expanded = triggerExpanded, onDismissRequest = { triggerExpanded = false }) {
                KeyTriggerTiming.entries.forEach { trigger ->
                    DropdownMenuItem(
                        text = { Text(trigger.displayName.plainText) },
                        onClick = {
                            onValueChange(keybindSetting.copy(trigger = trigger))
                            triggerExpanded = false
                        },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = keybindSetting.trigger == KeyTriggerTiming.LongPress || keybindSetting.trigger == KeyTriggerTiming.WhileLongPressed,
        ) {
            Column {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = longPressThresholdText,
                    onValueChange = {
                        longPressThresholdText = it.filter { c -> c.isDigit() }
                        longPressThresholdText.toIntOrNull()?.let { value ->
                            onValueChange(keybindSetting.copy(longPressThreshold = value))
                        }
                    },
                    label = { Text("Long Press Threshold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        AnimatedVisibility(
            visible = keybindSetting.trigger == KeyTriggerTiming.WhilePressed || keybindSetting.trigger == KeyTriggerTiming.WhileLongPressed,
        ) {
            Column {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = repeatIntervalText,
                    onValueChange = {
                        repeatIntervalText = it.filter { c -> c.isDigit() }
                        repeatIntervalText.toIntOrNull()?.let { value ->
                            onValueChange(keybindSetting.copy(repeatInterval = value))
                        }
                    },
                    label = { Text("Repeat Interval") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}