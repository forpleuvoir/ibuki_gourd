package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.text.translateText
import moe.forpleuvoir.ibukigourd.ui.icon.EditNote
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.platformcontext.MinecraftClipboard
import moe.forpleuvoir.ibukigourd.ui.preset.*
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.debug
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.ConfigRange
import moe.forpleuvoir.nebula.config.pathWithRoot
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.times
import kotlin.time.toDuration

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
        ((v - range.start) / (range.endInclusive - range.start)).toFloat() else 0f

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

    Row(
        modifier = Modifier.size(ConfigRowWrapper.entrySize),
        horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (range != null) {
            val scope = rememberCoroutineScope()
            val sliderAnim = remember { Animatable(fraction(value)) }
            LaunchedEffect(value) {
                sliderAnim.animateTo(fraction(value), tween(durationMillis = 200))
            }
            val sliderValue = (range.start + sliderAnim.value.toDouble() * (range.endInclusive - range.start))
            DurationSlider(
                value = sliderValue,
                onValueChange = {
                    config.setValue(it)
                    value = it
                    scope.launch { sliderAnim.snapTo(fraction(it)) }
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
            Icon(Icons.EditNote, IGLang.edit.plainText)
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
                title = { Text(config.translateText.plainText) },
                text = {
                    CompositionLocalProvider(
                        LocalClipboard provides MinecraftClipboard,
                        LocalNumberFieldStyle provides NumberFieldStyle.Outlined,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
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
                            }, modifier = Modifier.widthIn(min = 100.dp))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        config.setValue(duration)
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