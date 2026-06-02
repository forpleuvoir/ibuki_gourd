package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlin.time.Duration.Companion.milliseconds

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
            val savedValue = value
            delay(interval)
            val newValue = config.getValue()
            if (newValue != value && savedValue == value) {
                value = newValue
            }
        }
    }
    Switch(
        value,
        {
            config.setValue(it)
            value = it
        },
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        ) {
                            val scrollState = rememberScrollState()
                            OutlinedTextField(
                                state = state,
                                scrollState = scrollState,
                                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                            )
                            VerticalScrollbar(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                adapter = rememberScrollbarAdapter(scrollState),
                                style = defaultScrollbarStyle().copy(
                                    hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                )
                            )
                        }
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
    valueDisplay: (Int) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) = ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment) {
    CompositionLocalProvider(LocalNumberFieldStyle provides NumberFieldStyle.Outlined) {
        var value by remember { mutableStateOf(config.getValue()) }
        val range = remember { (config as? ConfigRange<Int>)?.let { it.minValue..it.maxValue } }
        val interval = ConfigRowWrapper.valuePollInterval

        fun fraction(v: Int) = if (range != null && range.last != range.first)
            (v - range.first).toFloat() / (range.last - range.first) else 0f

        val scope = rememberCoroutineScope()

        LaunchedEffect(config.pathWithRoot) {
            while (isActive) {
                delay(interval)
                val newValue = config.getValue()
                if (newValue != value) {
                    value = newValue
                }
            }
        }

        Row(
            modifier = Modifier.size(ConfigRowWrapper.entrySize),
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var editor by remember { mutableStateOf(range == null || range.last - range.first >= 1000) }
            AnimatedContent(
                targetState = editor,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    (slideInVertically() + fadeIn()) togetherWith (slideOutVertically() + fadeOut())
                },
                label = "editor_animation"
            ) { currentEditor ->
                if (currentEditor) {
                    IntField(
                        value = value,
                        onValueChange = {
                            config.setValue(it)
                            value = it
                        },
                        range = range,
                        valueDisplay = valueDisplay,
                        modifier = Modifier.weight(1f),
                        label = { Text(if (range != null) "Int [$range]" else "Int") }
                    )
                } else if (range != null) {
                    val sliderAnim = remember { Animatable(fraction(value)) }

                    LaunchedEffect(value) {
                        sliderAnim.animateTo(fraction(value), tween(durationMillis = 200))
                    }

                    val sliderValue = (range.first + sliderAnim.value * (range.last - range.first)).fastRoundToInt()
                    IntSlider(
                        value = sliderValue,
                        onValueChange = {
                            config.setValue(it)
                            value = it
                            scope.launch { sliderAnim.snapTo(fraction(it)) }
                        },
                        valueRange = range,
                        valueDisplay = valueDisplay,
                        modifier = Modifier.fillMaxWidth().height(24.dp).weight(1f),
                    )
                }
            }
            if (range != null) {
                val rotation = remember { Animatable(0f) }
                val duration = ConfigRowWrapper.iconAnimationDuration.inWholeMilliseconds.toInt()
                IconButton(onClick = {
                    editor = !editor
                    scope.launch { rotation.animateTo(if (editor) 0f else 180f, tween(duration)) }
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
        val range = remember { (config as? ConfigRange<Long>)?.let { it.minValue..it.maxValue } }
        val interval = ConfigRowWrapper.valuePollInterval

        fun fraction(v: Long) = if (range != null && range.last != range.first)
            (v - range.first).toFloat() / (range.last - range.first) else 0f

        val scope = rememberCoroutineScope()

        LaunchedEffect(config.pathWithRoot) {
            while (isActive) {
                delay(interval)
                val newValue = config.getValue()
                if (newValue != value) {
                    value = newValue
                }
            }
        }

        Row(
            modifier = Modifier.size(ConfigRowWrapper.entrySize),
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var editor by remember { mutableStateOf(range == null || range.last - range.first >= 1000L) }
            AnimatedContent(
                targetState = editor,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    (slideInVertically() + fadeIn()) togetherWith (slideOutVertically() + fadeOut())
                },
                label = "editor_animation"
            ) { currentEditor ->
                if (currentEditor) {
                    LongField(
                        value = value,
                        onValueChange = {
                            config.setValue(it)
                            value = it
                        },
                        range = range,
                        valueDisplay = valueDisplay,
                        modifier = Modifier.weight(1f),
                        label = { Text(if (range != null) "Long [$range]" else "Long") }
                    )
                } else if (range != null) {
                    val sliderAnim = remember { Animatable(fraction(value)) }

                    LaunchedEffect(value) {
                        sliderAnim.animateTo(fraction(value), tween(durationMillis = 200))
                    }

                    val sliderValue = (range.first + sliderAnim.value * (range.last - range.first)).toLong()
                    LongSlider(
                        value = sliderValue,
                        onValueChange = {
                            config.setValue(it)
                            value = it
                            scope.launch { sliderAnim.snapTo(fraction(it)) }
                        },
                        valueRange = range,
                        valueDisplay = valueDisplay,
                        modifier = Modifier.fillMaxWidth().height(24.dp).weight(1f),
                    )
                }
            }
            if (range != null) {
                val rotation = remember { Animatable(0f) }
                val duration = ConfigRowWrapper.iconAnimationDuration.inWholeMilliseconds.toInt()
                IconButton(onClick = {
                    editor = !editor
                    scope.launch { rotation.animateTo(if (editor) 0f else 180f, tween(duration)) }
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
        val range = remember { (config as? ConfigRange<Float>)?.let { it.minValue..it.maxValue } }
        val interval = ConfigRowWrapper.valuePollInterval

        fun fraction(v: Float) = if (range != null && range.endInclusive != range.start)
            (v - range.start) / (range.endInclusive - range.start) else 0f

        val scope = rememberCoroutineScope()

        LaunchedEffect(config.pathWithRoot) {
            while (isActive) {
                delay(interval)
                val newValue = config.getValue()
                if (newValue != value) {
                    value = newValue
                }
            }
        }

        Row(
            modifier = Modifier.size(ConfigRowWrapper.entrySize),
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var editor by remember { mutableStateOf(range == null || range.endInclusive - range.start >= 1000f) }
            AnimatedContent(
                targetState = editor,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    (slideInVertically() + fadeIn()) togetherWith (slideOutVertically() + fadeOut())
                },
                label = "editor_animation"
            ) { currentEditor ->
                if (currentEditor) {
                    FloatField(
                        value = value,
                        onValueChange = {
                            config.setValue(it)
                            value = it
                        },
                        range = range,
                        valueDisplay = valueDisplay,
                        modifier = Modifier.weight(1f),
                        label = { Text(if (range != null) "Float [$range]" else "Float") }
                    )
                } else if (range != null) {
                    val sliderAnim = remember { Animatable(fraction(value)) }

                    LaunchedEffect(value) {
                        sliderAnim.animateTo(fraction(value), tween(durationMillis = 200))
                    }

                    val sliderValue = range.start + sliderAnim.value * (range.endInclusive - range.start)
                    FloatSlider(
                        value = sliderValue,
                        onValueChange = {
                            config.setValue(it)
                            value = it
                            scope.launch { sliderAnim.snapTo(fraction(it)) }
                        },
                        valueRange = range,
                        valueDisplay = { valueDisplay("%.2f".format(it).toFloat()) },
                        modifier = Modifier.fillMaxWidth().height(24.dp).weight(1f),
                    )
                }
            }
            if (range != null) {
                val rotation = remember { Animatable(0f) }
                val duration = ConfigRowWrapper.iconAnimationDuration.inWholeMilliseconds.toInt()
                IconButton(onClick = {
                    editor = !editor
                    scope.launch { rotation.animateTo(if (editor) 0f else 180f, tween(duration)) }
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
        val range = remember { (config as? ConfigRange<Double>)?.let { it.minValue..it.maxValue } }
        val interval = ConfigRowWrapper.valuePollInterval

        fun fraction(v: Double) = if (range != null && range.endInclusive != range.start)
            ((v - range.start) / (range.endInclusive - range.start)).toFloat() else 0f

        val scope = rememberCoroutineScope()

        LaunchedEffect(config.pathWithRoot) {
            while (isActive) {
                delay(interval)
                val newValue = config.getValue()
                if (newValue != value) {
                    value = newValue
                }
            }
        }

        Row(
            modifier = Modifier.size(ConfigRowWrapper.entrySize),
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var editor by remember { mutableStateOf(range == null || range.endInclusive - range.start >= 1000.0) }
            AnimatedContent(
                targetState = editor,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    (slideInVertically() + fadeIn()) togetherWith (slideOutVertically() + fadeOut())
                },
                label = "editor_animation"
            ) { currentEditor ->
                if (currentEditor) {
                    DoubleField(
                        value = value,
                        onValueChange = {
                            config.setValue(it)
                            value = it
                        },
                        range = range,
                        valueDisplay = valueDisplay,
                        modifier = Modifier.weight(1f),
                        label = { Text(if (range != null) "Double [$range]" else "Double") }
                    )
                } else if (range != null) {
                    val sliderAnim = remember { Animatable(fraction(value)) }

                    LaunchedEffect(value) {
                        sliderAnim.animateTo(fraction(value), tween(durationMillis = 200))
                    }

                    val sliderValue = range.start + sliderAnim.value * (range.endInclusive - range.start)
                    DoubleSlider(
                        value = sliderValue,
                        onValueChange = {
                            config.setValue(it)
                            value = it
                            scope.launch { sliderAnim.snapTo(fraction(it)) }
                        },
                        valueRange = range,
                        valueDisplay = { valueDisplay("%.2f".format(it).toDouble()) },
                        modifier = Modifier.fillMaxWidth().height(24.dp).weight(1f),
                    )
                }
            }
            if (range != null) {
                val rotation = remember { Animatable(0f) }
                val duration = ConfigRowWrapper.iconAnimationDuration.inWholeMilliseconds.toInt()
                IconButton(onClick = {
                    editor = !editor
                    scope.launch { rotation.animateTo(if (editor) 0f else 180f, tween(duration)) }
                }) {
                    Icon(Icons.SyncAlt, null, modifier = Modifier.rotate(rotation.value))
                }
            }
        }
    }
}
