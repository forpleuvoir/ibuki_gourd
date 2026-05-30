@file:Suppress("DuplicatedCode")

package moe.forpleuvoir.ibukigourd.ui.preset

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object NumberSlider {

    val labelDismissDelay: Duration
        @Composable @ReadOnlyComposable get() = LocalNumberSliderLabelDismissDelay.current

    val LocalNumberSliderLabelDismissDelay = staticCompositionLocalOf {
        500.milliseconds
    }

    val labelAnimationDuration: Int
        @Composable @ReadOnlyComposable get() = LocalNumberSliderLabelAnimationDuration.current

    val LocalNumberSliderLabelAnimationDuration = staticCompositionLocalOf {
        200
    }

    val trackGap: Dp
        @Composable @ReadOnlyComposable get() = LocalNumberSliderTrackGap.current

    val LocalNumberSliderTrackGap = staticCompositionLocalOf { 4.dp }

    val alwaysShowLabel: Boolean
        @Composable @ReadOnlyComposable get() = LocalNumberSliderAlwaysShowLabel.current

    val LocalNumberSliderAlwaysShowLabel = staticCompositionLocalOf { true }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Track(
        state: SliderState,
        colors: SliderColors = SliderDefaults.colors(),
    ) {
        Box(Modifier.fillMaxWidth().height(32.dp).padding(horizontal = 1.dp).drawBehind {
            val fraction = state.value
            val gap = 6.dp.toPx()
            val thumb = size.width * fraction
            drawRoundRect(
                topLeft = Offset(thumb + gap, 0f),
                color = colors.inactiveTrackColor,
                size = Size(size.width - thumb - gap, size.height),
                cornerRadius = CornerRadius(16.dp.toPx())
            )
            drawRoundRect(
                color = colors.activeTrackColor,
                size = Size(thumb - gap, size.height),
                cornerRadius = CornerRadius(16.dp.toPx())
            )
        })
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    valueDisplay: (Int) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(colors = colors, enabled = enabled, sliderState = sliderState, thumbTrackGapSize = NumberSlider.trackGap)
    }
) {
    val textMeasurer = rememberTextMeasurer()

    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.inverseOnSurface)

    val bgColor = MaterialTheme.colorScheme.inverseSurface

    val isHovered by interactionSource.collectIsHoveredAsState()

    var showLabel by remember { mutableStateOf(false) }

    val delay = NumberSlider.labelDismissDelay

    val span = (valueRange.last - valueRange.first)

    val animatedAlpha by animateFloatAsState(
        targetValue = if(showLabel || NumberSlider.alwaysShowLabel) 1f else 0f,
        animationSpec = tween(durationMillis = NumberSlider.labelAnimationDuration),
        label = "labelAlpha",
    )

    var initial by remember { mutableStateOf(true) }
    LaunchedEffect(value, isHovered) {
        if (initial) {
            initial = false
            return@LaunchedEffect
        }
        if (isHovered) {
            showLabel = true
        } else {
            showLabel = true
            delay(delay)
            showLabel = false
        }
    }
    Slider(
        value = (value.toFloat() / span).coerceIn(0f, 1f),
        onValueChange = {
            onValueChange((valueRange.first + (span * it).fastRoundToInt()).coerceIn(valueRange))
        },
        interactionSource = interactionSource,
        modifier = modifier.hoverable(interactionSource),
        track = track,
        thumb = {
            SliderDefaults.Thumb(interactionSource, Modifier.drawBehind {
                if (animatedAlpha > 0.01f) {
                    val measuredText = textMeasurer.measure(
                        AnnotatedString(valueDisplay(value)),
                        style = labelStyle.copy(
                            color = labelStyle.color.copy(alpha = animatedAlpha)
                        ),
                    )
                    val bgWidth = measuredText.size.width + 12.dp.toPx()
                    val bgHeight = measuredText.size.height + 4.dp.toPx()
                    val bgX = -bgWidth / 2
                    val bgY = -bgHeight - 4.dp.toPx()
                    drawRoundRect(
                        color = bgColor.copy(alpha = animatedAlpha),
                        topLeft = Offset(bgX, bgY),
                        size = Size(bgWidth, bgHeight),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                    )
                    drawText(
                        textLayoutResult = measuredText,
                        topLeft = Offset(bgX + 6.dp.toPx(), bgY + 2.dp.toPx()),
                    )
                }
            })
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LongSlider(
    value: Long,
    onValueChange: (Long) -> Unit,
    valueRange: LongRange,
    valueDisplay: (Long) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(colors = colors, enabled = enabled, sliderState = sliderState, thumbTrackGapSize = NumberSlider.trackGap)
    }
) {
    val textMeasurer = rememberTextMeasurer()

    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.inverseOnSurface)

    val bgColor = MaterialTheme.colorScheme.inverseSurface

    val isHovered by interactionSource.collectIsHoveredAsState()

    var showLabel by remember { mutableStateOf(false) }

    val delay = NumberSlider.labelDismissDelay

    val span = (valueRange.last - valueRange.first)

    val animatedAlpha by animateFloatAsState(
        targetValue = if(showLabel || NumberSlider.alwaysShowLabel) 1f else 0f,
        animationSpec = tween(durationMillis = NumberSlider.labelAnimationDuration),
        label = "labelAlpha",
    )

    var initial by remember { mutableStateOf(true) }
    LaunchedEffect(value, isHovered) {
        if (initial) {
            initial = false
            return@LaunchedEffect
        }
        if (isHovered) {
            showLabel = true
        } else {
            showLabel = true
            delay(delay)
            showLabel = false
        }
    }
    Slider(
        value = (value.toFloat() / span).coerceIn(0f, 1f),
        onValueChange = {
            onValueChange((valueRange.first + (span * it).toLong()).coerceIn(valueRange))
        },
        interactionSource = interactionSource,
        modifier = modifier.hoverable(interactionSource),
        track = track,
        thumb = {
            SliderDefaults.Thumb(interactionSource, Modifier.drawBehind {
                if (animatedAlpha > 0.01f) {
                    val measuredText = textMeasurer.measure(
                        AnnotatedString(valueDisplay(value)),
                        style = labelStyle.copy(
                            color = labelStyle.color.copy(alpha = animatedAlpha)
                        ),
                    )
                    val bgWidth = measuredText.size.width + 12.dp.toPx()
                    val bgHeight = measuredText.size.height + 4.dp.toPx()
                    val bgX = -bgWidth / 2
                    val bgY = -bgHeight - 4.dp.toPx()
                    drawRoundRect(
                        color = bgColor.copy(alpha = animatedAlpha),
                        topLeft = Offset(bgX, bgY),
                        size = Size(bgWidth, bgHeight),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                    )
                    drawText(
                        textLayoutResult = measuredText,
                        topLeft = Offset(bgX + 6.dp.toPx(), bgY + 2.dp.toPx()),
                    )
                }
            })
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueDisplay: (Float) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(colors = colors, enabled = enabled, sliderState = sliderState, thumbTrackGapSize = NumberSlider.trackGap)
    }
) {
    val textMeasurer = rememberTextMeasurer()

    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.inverseOnSurface)

    val bgColor = MaterialTheme.colorScheme.inverseSurface

    val isHovered by interactionSource.collectIsHoveredAsState()

    var showLabel by remember { mutableStateOf(false) }

    val delay = NumberSlider.labelDismissDelay

    val animatedAlpha by animateFloatAsState(
        targetValue = if(showLabel || NumberSlider.alwaysShowLabel) 1f else 0f,
        animationSpec = tween(durationMillis = NumberSlider.labelAnimationDuration),
        label = "labelAlpha",
    )

    var initial by remember { mutableStateOf(true) }
    LaunchedEffect(value, isHovered) {
        if (initial) {
            initial = false
            return@LaunchedEffect
        }
        if (isHovered) {
            showLabel = true
        } else {
            showLabel = true
            delay(delay)
            showLabel = false
        }
    }
    Slider(
        value = value,
        onValueChange = { onValueChange(it) },
        valueRange = valueRange,
        interactionSource = interactionSource,
        modifier = modifier.hoverable(interactionSource),
        track = track,
        thumb = {
            SliderDefaults.Thumb(interactionSource, Modifier.drawBehind {
                if (animatedAlpha > 0.01f) {
                    val measuredText = textMeasurer.measure(
                        AnnotatedString(valueDisplay(value)),
                        style = labelStyle.copy(
                            color = labelStyle.color.copy(alpha = animatedAlpha)
                        ),
                    )
                    val bgWidth = measuredText.size.width + 12.dp.toPx()
                    val bgHeight = measuredText.size.height + 4.dp.toPx()
                    val bgX = -bgWidth / 2
                    val bgY = -bgHeight - 4.dp.toPx()
                    drawRoundRect(
                        color = bgColor.copy(alpha = animatedAlpha),
                        topLeft = Offset(bgX, bgY),
                        size = Size(bgWidth, bgHeight),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                    )
                    drawText(
                        textLayoutResult = measuredText,
                        topLeft = Offset(bgX + 6.dp.toPx(), bgY + 2.dp.toPx()),
                    )
                }
            })
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubleSlider(
    value: Double,
    onValueChange: (Double) -> Unit,
    valueRange: ClosedFloatingPointRange<Double>,
    valueDisplay: (Double) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(colors = colors, enabled = enabled, sliderState = sliderState, thumbTrackGapSize = NumberSlider.trackGap)
    }
) {
    val textMeasurer = rememberTextMeasurer()

    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.inverseOnSurface)

    val bgColor = MaterialTheme.colorScheme.inverseSurface

    val isHovered by interactionSource.collectIsHoveredAsState()

    var showLabel by remember { mutableStateOf(false) }

    val delay = NumberSlider.labelDismissDelay

    val span = valueRange.endInclusive - valueRange.start

    val animatedAlpha by animateFloatAsState(
        targetValue = if(showLabel || NumberSlider.alwaysShowLabel) 1f else 0f,
        animationSpec = tween(durationMillis = NumberSlider.labelAnimationDuration),
        label = "labelAlpha",
    )

    var initial by remember { mutableStateOf(true) }
    LaunchedEffect(value, isHovered) {
        if (initial) {
            initial = false
            return@LaunchedEffect
        }
        if (isHovered) {
            showLabel = true
        } else {
            showLabel = true
            delay(delay)
            showLabel = false
        }
    }
    Slider(
        value = (value / span).toFloat().coerceIn(0f, 1f),
        onValueChange = {
            onValueChange((valueRange.start + span * it).coerceIn(valueRange))
        },
        interactionSource = interactionSource,
        modifier = modifier.hoverable(interactionSource),
        track = track,
        thumb = {
            SliderDefaults.Thumb(interactionSource, Modifier.drawBehind {
                if (animatedAlpha > 0.01f) {
                    val measuredText = textMeasurer.measure(
                        AnnotatedString(valueDisplay(value)),
                        style = labelStyle.copy(
                            color = labelStyle.color.copy(alpha = animatedAlpha)
                        ),
                    )
                    val bgWidth = measuredText.size.width + 12.dp.toPx()
                    val bgHeight = measuredText.size.height + 4.dp.toPx()
                    val bgX = -bgWidth / 2
                    val bgY = -bgHeight - 4.dp.toPx()
                    drawRoundRect(
                        color = bgColor.copy(alpha = animatedAlpha),
                        topLeft = Offset(bgX, bgY),
                        size = Size(bgWidth, bgHeight),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                    )
                    drawText(
                        textLayoutResult = measuredText,
                        topLeft = Offset(bgX + 6.dp.toPx(), bgY + 2.dp.toPx()),
                    )
                }
            })
        }
    )
}
