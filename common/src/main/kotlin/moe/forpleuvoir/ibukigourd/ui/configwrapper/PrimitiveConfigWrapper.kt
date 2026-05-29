package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.EditNote
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.SyncAlt
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftClipboard
import moe.forpleuvoir.ibukigourd.ui.preset.*
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.ConfigRange
import moe.forpleuvoir.nebula.config.pathWithRoot

@Composable
fun BooleanConfigWrapper(
    config: Config<Boolean>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) = ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment) {
    var value by remember { mutableStateOf(config.getValue()) }

    val interval = ConfigRowWrapper.valuePollInterval
    LaunchedEffect(config.pathWithRoot) {
        while (isActive) {
            value = config.getValue()
            delay(interval)
        }
    }
    Switch(
        value,
        { config.setValue(it) },
        modifier = modifier.height(ConfigRowWrapper.entrySize.height),
    )
}

@Composable
fun StringConfigWrapper(
    config: Config<String>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    val textFieldState = rememberTextFieldState(config.getValue())

    ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment, onReset = {
        textFieldState.setTextAndPlaceCursorAtEnd(config.getValue())
    }) {
        var showDialog by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.size(ConfigRowWrapper.entrySize),
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            LaunchedEffect(textFieldState.text) {
                config.setValue(textFieldState.text.toString())
            }

            OutlinedTextField(
                state = textFieldState,
                label = { Text("String") },
                lineLimits = TextFieldLineLimits.SingleLine,
                modifier = Modifier.weight(1f),
            )

            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.EditNote, IGLang.edit.plainText)
            }
        }
        if (showDialog) {
            val state = rememberTextFieldState(config.getValue())
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(config.translateText.plainText) },
                text = {
                    CompositionLocalProvider(LocalClipboard provides MinecraftClipboard) {
                        OutlinedTextField(
                            state = state,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp),
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        config.setValue(state.text.toString())
                        textFieldState.setTextAndPlaceCursorAtEnd(state.text.toString())
                        showDialog = false
                    }) {
                        Text(IGLang.confirm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(IGLang.cancel)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntConfigWrapper(
    config: Config<Int>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    valueDisplay: (Int) -> String = { it.toString() },
) = ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment) {
    CompositionLocalProvider(LocalNumberFieldStyle provides NumberFieldStyle.Outlined) {
        var value by remember { mutableStateOf(config.getValue()) }
        val range = remember { if (config is ConfigRange<Int>) config.minValue..config.maxValue else null }

        val interval = ConfigRowWrapper.valuePollInterval

        val sliderAnim = remember { Animatable(value.toFloat()) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(config.pathWithRoot) {
            while (isActive) {
                val newValue = config.getValue()
                if (newValue != value && range != null) {
                    value = newValue
                    val fraction = (newValue - range.first).toFloat() / (range.last - range.first)
                    sliderAnim.animateTo(
                        targetValue = fraction,
                        animationSpec = tween(durationMillis = 200)
                    )
                }
                delay(interval)
            }
        }


        Row(
            modifier = Modifier.size(ConfigRowWrapper.entrySize),
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var editor by remember { mutableStateOf(!(range != null && (range.first - range.last) < 1000)) }
            if (editor) {
                IntField(
                    value = value,
                    onValueChange = {
                        config.setValue(it)
                        value = it
                    },
                    range = range,
                    valueDisplay = valueDisplay,
                    modifier = modifier.weight(1f),
                    label = {
                        Text("Int [$range]")
                    }
                )
            } else if (range != null) {
                val sliderValue = (range.first + sliderAnim.value * (range.last - range.first)).fastRoundToInt()
                IntSlider(
                    value = sliderValue,
                    onValueChange = {
                        config.setValue(it)
                        scope.launch {
                            val fraction = (it - range.first).toFloat() / (range.last - range.first)
                            sliderAnim.snapTo(fraction)
                        }
                        value = it
                    },
                    valueRange = range,
                    valueDisplay = valueDisplay,
                    modifier = Modifier.fillMaxWidth().height(24.dp).weight(1f),
                )
            }
            if (range != null) {
                val rotation = remember { Animatable(0f) }
                val scope = rememberCoroutineScope()
                val duration = ConfigRowWrapper.iconAnimationDuration.inWholeMilliseconds.toInt()
                val target = if (editor) 0f else 180f
                IconButton(onClick = {
                    editor = !editor
                    scope.launch {
                        rotation.animateTo(
                            targetValue = target,
                            animationSpec = tween(duration)
                        )
                    }
                }) {
                    Icon(Icons.SyncAlt, null, modifier = Modifier.rotate(rotation.value))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LongConfigWrapper(
    config: Config<Long>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    valueDisplay: (Long) -> String = { it.toString() },
) = ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment) {
    CompositionLocalProvider(LocalNumberFieldStyle provides NumberFieldStyle.Outlined) {
        var value by remember { mutableStateOf(config.getValue()) }
        val range = remember { if (config is ConfigRange<Long>) config.minValue..config.maxValue else null }

        val interval = ConfigRowWrapper.valuePollInterval
        LaunchedEffect(config.pathWithRoot) {
            while (isActive) {
                value = config.getValue()
                delay(interval)
            }
        }
        Row(
            modifier = Modifier.size(ConfigRowWrapper.entrySize),
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var editor by remember { mutableStateOf(!(range != null && (range.first - range.last) < 1000)) }
            if (editor) {
                LongField(
                    value = value,
                    onValueChange = {
                        config.setValue(it)
                        value = it
                    },
                    range = range,
                    valueDisplay = valueDisplay,
                    modifier = modifier.weight(1f),
                    label = {
                        Text("Long [$range]")
                    }
                )
            } else if (range != null) {
                LongSlider(
                    value = value,
                    onValueChange = {
                        config.setValue(it)
                        value = it
                    },
                    valueRange = range,
                    valueDisplay = valueDisplay,
                    modifier = Modifier.fillMaxWidth().height(24.dp).weight(1f),
                )
            }
            if (range != null) {
                val rotation = remember { Animatable(0f) }
                val scope = rememberCoroutineScope()
                val duration = ConfigRowWrapper.iconAnimationDuration.inWholeMilliseconds.toInt()
                val target = if (editor) 0f else 180f
                IconButton(onClick = {
                    editor = !editor
                    scope.launch {
                        rotation.animateTo(
                            targetValue = target,
                            animationSpec = tween(duration)
                        )
                    }
                }) {
                    Icon(Icons.SyncAlt, null, modifier = Modifier.rotate(rotation.value))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatConfigWrapper(
    config: Config<Float>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    valueDisplay: (Float) -> String = { it.toString() },
) = ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment) {
    CompositionLocalProvider(LocalNumberFieldStyle provides NumberFieldStyle.Outlined) {
        var value by remember { mutableStateOf(config.getValue()) }
        val range = remember { if (config is ConfigRange<Float>) config.minValue..config.maxValue else null }

        val interval = ConfigRowWrapper.valuePollInterval
        LaunchedEffect(config.pathWithRoot) {
            while (isActive) {
                value = config.getValue()
                delay(interval)
            }
        }
        Row(
            modifier = Modifier.size(ConfigRowWrapper.entrySize),
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var editor by remember { mutableStateOf(!(range != null && (range.start - range.endInclusive) < 1000)) }
            if (editor) {
                FloatField(
                    value = value,
                    onValueChange = {
                        config.setValue(it)
                        value = it
                    },
                    range = range,
                    valueDisplay = valueDisplay,
                    modifier = modifier.weight(1f),
                    label = {
                        Text("Float [$range]")
                    }
                )
            } else if (range != null) {
                FloatSlider(
                    value = value,
                    onValueChange = {
                        config.setValue(it)
                        value = it
                    },
                    valueRange = range,
                    valueDisplay = { valueDisplay("%.2f".format(it).toFloat()) },
                    modifier = Modifier.fillMaxWidth().height(24.dp).weight(1f),
                )
            }
            if (range != null) {
                val rotation = remember { Animatable(0f) }
                val scope = rememberCoroutineScope()
                val duration = ConfigRowWrapper.iconAnimationDuration.inWholeMilliseconds.toInt()
                val target = if (editor) 0f else 180f
                IconButton(onClick = {
                    editor = !editor
                    scope.launch {
                        rotation.animateTo(
                            targetValue = target,
                            animationSpec = tween(duration)
                        )
                    }
                }) {
                    Icon(Icons.SyncAlt, null, modifier = Modifier.rotate(rotation.value))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubleConfigWrapper(
    config: Config<Double>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    valueDisplay: (Double) -> String = { it.toString() },
) = ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment) {
    CompositionLocalProvider(LocalNumberFieldStyle provides NumberFieldStyle.Outlined) {
        var value by remember { mutableStateOf(config.getValue()) }
        val range = remember { if (config is ConfigRange<Double>) config.minValue..config.maxValue else null }

        val interval = ConfigRowWrapper.valuePollInterval
        LaunchedEffect(config.pathWithRoot) {
            while (isActive) {
                value = config.getValue()
                delay(interval)
            }
        }
        Row(
            modifier = Modifier.size(ConfigRowWrapper.entrySize),
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var editor by remember { mutableStateOf(!(range != null && (range.start - range.endInclusive) < 1000)) }
            if (editor) {
                DoubleField(
                    value = value,
                    onValueChange = {
                        config.setValue(it)
                        value = it
                    },
                    range = range,
                    valueDisplay = valueDisplay,
                    modifier = modifier.weight(1f),
                    label = {
                        Text("Double [$range]")
                    }
                )
            } else if (range != null) {
                DoubleSlider(
                    value = value,
                    onValueChange = {
                        config.setValue(it)
                        value = it
                    },
                    valueRange = range,
                    valueDisplay = { valueDisplay("%.2f".format(it).toDouble()) },
                    modifier = Modifier.fillMaxWidth().height(24.dp).weight(1f),
                )
            }
            if (range != null) {
                val rotation = remember { Animatable(0f) }
                val scope = rememberCoroutineScope()
                val duration = ConfigRowWrapper.iconAnimationDuration.inWholeMilliseconds.toInt()
                val target = if (editor) 0f else 180f
                IconButton(onClick = {
                    editor = !editor
                    scope.launch {
                        rotation.animateTo(
                            targetValue = target,
                            animationSpec = tween(duration)
                        )
                    }
                }) {
                    Icon(Icons.SyncAlt, null, modifier = Modifier.rotate(rotation.value))
                }
            }
        }
    }
}
