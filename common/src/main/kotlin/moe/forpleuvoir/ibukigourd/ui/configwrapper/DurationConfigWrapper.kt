package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.text.translateText
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.EditNote
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IGCompositionLocalProvider
import moe.forpleuvoir.ibukigourd.ui.preset.*
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.ConfigRange
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.times
import kotlin.time.toDuration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DurationConfigWrapper(
    config: Config<Duration>,
    valueDisplay: (Duration) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) = ConfigRowWrapper(config, modifier, horizontalArrangement, verticalAlignment) {

    val range = if (config is ConfigRange<Duration>) {
        config.minValue..config.maxValue
    } else null

    fun fraction(v: Duration) = if (range != null && range.endInclusive != range.start)
        ((v - range.start) / (range.endInclusive - range.start)) else 0.0

    val value by config.asState()

    Row(
        modifier = Modifier.size(ConfigRowWrapper.entrySize),
        horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (range != null) {
            val scope = rememberCoroutineScope()
            val sliderAnim = remember { Animatable(fraction(value).toFloat()) }
            var initialized by remember { mutableStateOf(false) }
            var targetValue by remember { mutableStateOf(value) }
            var isDragging by remember { mutableStateOf(false) }

            LaunchedEffect(value) {
                if (!initialized) {
                    sliderAnim.snapTo(fraction(value).toFloat())
                    initialized = true
                    targetValue = value
                } else if (!isDragging) {
                    sliderAnim.animateTo(fraction(value).toFloat(), tween(durationMillis = 200))
                    targetValue = value
                }
            }

            val sliderValue = when {
                isDragging -> value
                sliderAnim.isRunning -> range.start + sliderAnim.value.toDouble() * (range.endInclusive - range.start)
                else -> targetValue
            }

            DurationSlider(
                value = sliderValue,
                onValueChange = {
                    isDragging = true
                    config.setValue(it)
                },
                onValueChangeFinished = {
                    scope.launch {
                        sliderAnim.snapTo(fraction(value).toFloat())
                        isDragging = false
                        targetValue = value
                    }
                },
                valueRange = range,
                valueDisplay = valueDisplay,
                modifier = Modifier.weight(1f).height(24.dp).weight(1f),
            )
        } else {
            AssistChip({}, {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(valueDisplay(value), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }, modifier = Modifier.weight(1f).height(40.dp))
        }
        var showDialog by remember { mutableStateOf(false) }
        IconButton(onClick = { showDialog = true }) {
            Icon(Icons.EditNote, IGLang.Misc.edit.plainText)
        }

        if (showDialog) {
            var unit by remember { mutableStateOf(DurationUnit.SECONDS) }
            var state by remember { mutableStateOf(config.getValue().toDouble(unit)) }
            var durationRange by remember {
                mutableStateOf(if (range != null) range.start.toDouble(unit)..range.endInclusive.toDouble(unit) else null)
            }
            var duration by remember { mutableStateOf(state.toDuration(unit)) }
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(InlineStyleText(config.translateText.plainText)) },
                text = {
                    IGCompositionLocalProvider(LocalNumberFieldStyle provides NumberFieldStyle.Outlined) {
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            DoubleField(
                                state,
                                onValueChange = {
                                    state = it
                                    duration = state.toDuration(unit)
                                },
                                label = {
                                    Text(durationRange?.let { "${it.start}..${it.endInclusive} ${unit.translateText.plainText}" }
                                        ?: unit.translateText.plainText)
                                },
                                range = durationRange,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(12.dp))
                            EnumSelector(unit, {
                                unit = it
                                state = duration.toDouble(unit)
                                durationRange = if (range != null) range.start.toDouble(unit)..range.endInclusive.toDouble(unit) else null
                            }, modifier = Modifier.width(100.dp))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        config.setValue(duration)
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