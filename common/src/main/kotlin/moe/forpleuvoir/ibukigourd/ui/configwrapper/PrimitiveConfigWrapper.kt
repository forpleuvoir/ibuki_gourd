@file:Suppress("DuplicatedCode")

package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.VerticalScrollbar
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.EditNote
import moe.forpleuvoir.ibukigourd.ui.icon.default.SyncAlt
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IGCompositionLocalProvider
import moe.forpleuvoir.ibukigourd.ui.preset.*
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.ConfigRange

@Composable
fun BooleanConfigWrapper(
    config: Config<Boolean>,
    thumbContent: (@Composable (Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) = ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment) {
    val value by config.asState()
    Switch(
        value,
        {
            config.setValue(it)
        },
        modifier = modifier.height(ConfigRowWrapper.entrySize.height),
        thumbContent = thumbContent?.let { { it(value) } }
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
                Icon(Icons.EditNote, IGLang.Misc.edit.plainText)
            }
        }
        if (showDialog) {
            val state = rememberTextFieldState(config.getValue())
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(InlineStyleText(config.translateText.plainText)) },
                text = {
                    IGCompositionLocalProvider {
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
                                adapter = rememberScrollbarAdapter(scrollState)
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
                        Text(IGLang.Misc.confirm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(IGLang.Misc.cancel)
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
        val value by config.asState()
        val range = remember { (config as? ConfigRange<Int>)?.let { it.minValue..it.maxValue } }

        fun fraction(v: Int) = if (range != null && range.last != range.first)
            (v - range.first).toFloat() / (range.last - range.first) else 0f

        val scope = rememberCoroutineScope()

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
                        },
                        range = range,
                        valueDisplay = valueDisplay,
                        modifier = Modifier.weight(1f),
                        label = { Text(if (range != null) "Int [$range]" else "Int") }
                    )
                } else if (range != null) {
                    val sliderAnim = remember { Animatable(fraction(value)) }
                    var initialized by remember { mutableStateOf(false) }
                    var targetValue by remember { mutableStateOf(value) }
                    var isDragging by remember { mutableStateOf(false) }

                    LaunchedEffect(value) {
                        if (!initialized) {
                            sliderAnim.snapTo(fraction(value))
                            initialized = true
                            targetValue = value
                        } else if (!isDragging) {
                            sliderAnim.animateTo(fraction(value), tween(durationMillis = 200))
                            targetValue = value
                        }
                    }

                    val sliderValue = when {
                        isDragging           -> value
                        sliderAnim.isRunning -> (range.first + sliderAnim.value * (range.last - range.first)).fastRoundToInt()
                        else                 -> targetValue
                    }
                    IntSlider(
                        value = sliderValue,
                        onValueChange = {
                            isDragging = true
                            config.setValue(it)
                        },
                        onValueChangeFinished = {
                            scope.launch {
                                sliderAnim.snapTo(fraction(value))
                                isDragging = false
                                targetValue = value
                            }
                        },
                        valueRange = range,
                        valueDisplay = valueDisplay,
                        modifier = Modifier.fillMaxWidth().height(24.dp).weight(1f),
                    )
                }
            }
            if (range != null) {
                val rotation = remember { Animatable(if (editor) 0f else 180f) }
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
        val value by config.asState()
        val range = remember { (config as? ConfigRange<Long>)?.let { it.minValue..it.maxValue } }

        fun fraction(v: Long) = if (range != null && range.last != range.first)
            (v - range.first).toFloat() / (range.last - range.first) else 0f

        val scope = rememberCoroutineScope()
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
                        },
                        range = range,
                        valueDisplay = valueDisplay,
                        modifier = Modifier.weight(1f),
                        label = { Text(if (range != null) "Long [$range]" else "Long") }
                    )
                } else if (range != null) {
                    val sliderAnim = remember { Animatable(fraction(value)) }
                    var initialized by remember { mutableStateOf(false) }
                    var targetValue by remember { mutableStateOf(value) }
                    var isDragging by remember { mutableStateOf(false) }

                    LaunchedEffect(value) {
                        if (!initialized) {
                            sliderAnim.snapTo(fraction(value))
                            initialized = true
                            targetValue = value
                        } else if (!isDragging) {
                            sliderAnim.animateTo(fraction(value), tween(durationMillis = 200))
                            targetValue = value
                        }
                    }

                    val sliderValue: Long = when {
                        isDragging           -> value
                        sliderAnim.isRunning -> (range.first + sliderAnim.value * (range.last - range.first)).toLong()
                        else                 -> targetValue
                    }
                    LongSlider(
                        value = sliderValue,
                        onValueChange = {
                            isDragging = true
                            config.setValue(it)
                        },
                        onValueChangeFinished = {
                            scope.launch {
                                sliderAnim.snapTo(fraction(value))
                                isDragging = false
                                targetValue = value
                            }
                        },
                        valueRange = range,
                        valueDisplay = valueDisplay,
                        modifier = Modifier.fillMaxWidth().height(24.dp).weight(1f),
                    )
                }
            }
            if (range != null) {
                val rotation = remember { Animatable(if (editor) 0f else 180f) }
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
        val value by config.asState()
        val range = remember { (config as? ConfigRange<Float>)?.let { it.minValue..it.maxValue } }

        fun fraction(v: Float) = if (range != null && range.endInclusive != range.start)
            (v - range.start) / (range.endInclusive - range.start) else 0f

        val scope = rememberCoroutineScope()

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
                        },
                        range = range,
                        valueDisplay = valueDisplay,
                        modifier = Modifier.weight(1f),
                        label = { Text(if (range != null) "Float [$range]" else "Float") }
                    )
                } else if (range != null) {
                    val sliderAnim = remember { Animatable(fraction(value)) }
                    var initialized by remember { mutableStateOf(false) }
                    var targetValue by remember { mutableStateOf(value) }
                    var isDragging by remember { mutableStateOf(false) }

                    LaunchedEffect(value) {
                        if (!initialized) {
                            sliderAnim.snapTo(fraction(value))
                            initialized = true
                            targetValue = value
                        } else if (!isDragging) {
                            sliderAnim.animateTo(fraction(value), tween(durationMillis = 200))
                            targetValue = value
                        }
                    }

                    val sliderValue = when {
                        isDragging           -> value
                        sliderAnim.isRunning -> range.start + sliderAnim.value * (range.endInclusive - range.start)
                        else                 -> targetValue
                    }
                    FloatSlider(
                        value = sliderValue,
                        onValueChange = {
                            isDragging = true
                            config.setValue(it)
                        },
                        onValueChangeFinished = {
                            scope.launch {
                                sliderAnim.snapTo(fraction(value))
                                isDragging = false
                                targetValue = value
                            }
                        },
                        valueRange = range,
                        valueDisplay = { valueDisplay("%.2f".format(it).toFloat()) },
                        modifier = Modifier.fillMaxWidth().height(24.dp).weight(1f),
                    )
                }
            }
            if (range != null) {
                val rotation = remember { Animatable(if (editor) 0f else 180f) }
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
        val value by config.asState()
        val range = remember { (config as? ConfigRange<Double>)?.let { it.minValue..it.maxValue } }

        fun fraction(v: Double) = if (range != null && range.endInclusive != range.start)
            ((v - range.start) / (range.endInclusive - range.start)).toFloat() else 0f

        val scope = rememberCoroutineScope()

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
                        },
                        range = range,
                        valueDisplay = valueDisplay,
                        modifier = Modifier.weight(1f),
                        label = { Text(if (range != null) "Double [$range]" else "Double") }
                    )
                } else if (range != null) {
                    val sliderAnim = remember { Animatable(fraction(value)) }
                    var initialized by remember { mutableStateOf(false) }
                    var targetValue by remember { mutableStateOf(value) }
                    var isDragging by remember { mutableStateOf(false) }

                    LaunchedEffect(value) {
                        if (!initialized) {
                            sliderAnim.snapTo(fraction(value))
                            initialized = true
                            targetValue = value
                        } else if (!isDragging) {
                            sliderAnim.animateTo(fraction(value), tween(durationMillis = 200))
                            targetValue = value
                        }
                    }

                    val sliderValue = when {
                        isDragging           -> value
                        sliderAnim.isRunning -> range.start + sliderAnim.value * (range.endInclusive - range.start)
                        else                 -> targetValue
                    }
                    DoubleSlider(
                        value = sliderValue,
                        onValueChange = {
                            isDragging = true
                            config.setValue(it)
                        },
                        onValueChangeFinished = {
                            scope.launch {
                                sliderAnim.snapTo(fraction(value))
                                isDragging = false
                                targetValue = value
                            }
                        },
                        valueRange = range,
                        valueDisplay = { valueDisplay("%.2f".format(it).toDouble()) },
                        modifier = Modifier.fillMaxWidth().height(24.dp).weight(1f),
                    )
                }
            }
            if (range != null) {
                val rotation = remember { Animatable(if (editor) 0f else 180f) }
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
