package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

/**
 * 类型化滑条：在 [Slider] 之上做数值 ↔ 进度的双向换算与数值标签，对齐迁移前
 * `ui/preset/NumberSlider.kt`（Material3 版）的入口集合。
 *
 * 结构：[Slider] 负责渲染/手势/描边，本文件只补两件事——
 * 1. 把类型化 [valueRange] 折算成 Slider 的浮点区间，并把回调值换回类型
 *    （整数型自动设 [Slider] 的 steps 吸附到整数档）；
 * 2. 默认数值标签（[Text]），经 Slider 的两段裁剪获得"填充侧/轨道侧各一色"的效果，
 *    文本格式由各入口的 `valueToText` 决定。
 *
 * 精度说明：[Slider] 内部以 Float 承载进度，[DoubleSlider] / [DurationSlider] 的值经
 * Float 进度往返，只适合 UI 展示级精度（持久化请以调用方持有的原值为准）。
 */

/**
 * 整数滑条：进度自动吸附到 [valueRange] 的每个整数档（[Slider] steps = 档数 - 2）。
 *
 * @param value 当前值，绘制与语义均会收敛进 [valueRange]
 * @param onValueChange 值变化回调；null = 只读展示
 * @param valueRange 取值区间（含两端），last > first 才可滑动
 * @param valueToText 标签文本，默认 [Int.toString]
 */
@Composable
fun IntSlider(
    value: Int,
    onValueChange: ((Int) -> Unit)?,
    valueRange: IntRange,
    valueToText: (Int) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    trackSprite: SokitsuSprite = SliderDefaults.trackSprite(),
    fillSprite: SokitsuSprite = SliderDefaults.fillSprite(),
    interactionSource: MutableInteractionSource? = null,
) {
    val span = (valueRange.last - valueRange.first).coerceAtLeast(0)
    NumberSliderImpl(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
        steps = if (span > 0) span - 1 else 0,
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        trackSprite = trackSprite,
        fillSprite = fillSprite,
        interactionSource = interactionSource,
        toSliderValue = { it.toFloat() },
        fromSliderValue = { it.roundToInt().coerceIn(valueRange.first, valueRange.last) },
        label = { Text(valueToText(it)) },
    )
}

/**
 * 长整数滑条：行为同 [IntSlider]。
 *
 * @param valueRange 取值区间（含两端），last > first 才可滑动
 * @param valueToText 标签文本，默认 [Long.toString]
 */
@Composable
fun LongSlider(
    value: Long,
    onValueChange: ((Long) -> Unit)?,
    valueRange: LongRange,
    valueToText: (Long) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    trackSprite: SokitsuSprite = SliderDefaults.trackSprite(),
    fillSprite: SokitsuSprite = SliderDefaults.fillSprite(),
    interactionSource: MutableInteractionSource? = null,
) {
    val span = (valueRange.last - valueRange.first).coerceAtLeast(0L)
    NumberSliderImpl(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
        steps = if (span > 1L) (span - 1L).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt() else 0,
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        trackSprite = trackSprite,
        fillSprite = fillSprite,
        interactionSource = interactionSource,
        toSliderValue = { it.toFloat() },
        fromSliderValue = { it.roundToInt().toLong().coerceIn(valueRange.first, valueRange.last) },
        label = { Text(valueToText(it)) },
    )
}

/**
 * 浮点滑条：连续取值（不吸附）。
 *
 * @param value 当前值，绘制与语义均会收敛进 [valueRange]
 * @param onValueChange 值变化回调；null = 只读展示
 * @param valueRange 取值区间，endInclusive > start 才可滑动
 * @param valueToText 标签文本，默认 [Float.toString]
 */
@Composable
fun FloatSlider(
    value: Float,
    onValueChange: ((Float) -> Unit)?,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueToText: (Float) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    trackSprite: SokitsuSprite = SliderDefaults.trackSprite(),
    fillSprite: SokitsuSprite = SliderDefaults.fillSprite(),
    interactionSource: MutableInteractionSource? = null,
) {
    NumberSliderImpl(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = 0,
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        trackSprite = trackSprite,
        fillSprite = fillSprite,
        interactionSource = interactionSource,
        toSliderValue = { it },
        fromSliderValue = { it },
        label = { Text(valueToText(it)) },
    )
}

/**
 * 双精度浮点滑条：区间端点全程保持 [Double]（进度以 0..1 比例进出 Slider），
 * 换算损失只有 Float 进度本身的相对误差（~1e-7），端点不会被 Float 截断。
 *
 * @param valueToText 标签文本，默认 [Double.toString]
 */
@Composable
fun DoubleSlider(
    value: Double,
    onValueChange: ((Double) -> Unit)?,
    valueRange: ClosedFloatingPointRange<Double>,
    valueToText: (Double) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    trackSprite: SokitsuSprite = SliderDefaults.trackSprite(),
    fillSprite: SokitsuSprite = SliderDefaults.fillSprite(),
    interactionSource: MutableInteractionSource? = null,
) {
    val span = valueRange.endInclusive - valueRange.start
    NumberSliderImpl(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..1f,
        steps = 0,
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        trackSprite = trackSprite,
        fillSprite = fillSprite,
        interactionSource = interactionSource,
        toSliderValue = {
            if (span > 0.0) ((it - valueRange.start) / span).toFloat().coerceIn(0f, 1f) else 0f
        },
        fromSliderValue = { valueRange.start + span * it.toDouble() },
        label = { Text(valueToText(it)) },
    )
}

/**
 * 时长滑条：进度换算以毫秒为粒度（[Duration.inWholeMilliseconds]）。
 *
 * 默认把拖动结果吸附到整秒（[step] = 1 秒）：Slider 内部以 Float 承载进度，
 * 连续换算会产出一长串小数秒（`3m 2.52s`）。需要更细的档位时显式传 [step]
 * （如 `100.milliseconds`）；传 `Duration.ZERO` = 连续不吸附。
 *
 * @param value 当前值，绘制与语义均会收敛进 [valueRange]
 * @param onValueChange 值变化回调；null = 只读展示
 * @param valueRange 取值区间，endInclusive > start 才可滑动
 * @param step 拖动结果的吸附粒度，从 [valueRange.start] 起算；默认 1 秒
 * @param valueToText 标签文本，默认 [Duration.toString]（如 `1h 30m`）
 */
@Composable
fun DurationSlider(
    value: Duration,
    onValueChange: ((Duration) -> Unit)?,
    valueRange: ClosedRange<Duration>,
    step: Duration = 1.seconds,
    valueToText: (Duration) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    trackSprite: SokitsuSprite = SliderDefaults.trackSprite(),
    fillSprite: SokitsuSprite = SliderDefaults.fillSprite(),
    interactionSource: MutableInteractionSource? = null,
) {
    val span = valueRange.endInclusive - valueRange.start
    NumberSliderImpl(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..1f,
        steps = 0,
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        trackSprite = trackSprite,
        fillSprite = fillSprite,
        interactionSource = interactionSource,
        toSliderValue = {
            val spanMs = span.inWholeMilliseconds
            if (spanMs <= 0) 0f
            else ((it - valueRange.start).inWholeMilliseconds.toDouble() / spanMs)
                .toFloat().coerceIn(0f, 1f)
        },
        fromSliderValue = { f ->
            val offset = span * f.toDouble()
            if (step > Duration.ZERO) {
                // 按步长吸附（纳秒整除，避免再引入浮点误差），并收敛进区间
                val stepNs = step.inWholeNanoseconds.coerceAtLeast(1)
                val spanNs = span.inWholeNanoseconds.coerceAtLeast(0)
                val units = (offset.inWholeNanoseconds.toDouble() / stepNs).roundToInt()
                    .coerceIn(0, (spanNs / stepNs).toInt())
                valueRange.start + (units.toLong() * stepNs).nanoseconds
            } else {
                valueRange.start + offset
            }
        },
        label = { Text(valueToText(it)) },
    )
}

/**
 * 百分比滑条：标签按 [valueRange] 区间占比显示为整数百分比（`xx%`）。
 *
 * @param steps 传入区间等分数可让百分比按整数档吸附（如 0..1 传 99 = 每步 1%）
 * @param valueToText 标签文本；null = 区间占比四舍五入到整数百分比
 */
@Composable
fun PercentSlider(
    value: Float,
    onValueChange: ((Float) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    steps: Int = 0,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueToText: ((Float) -> String)? = null,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    trackSprite: SokitsuSprite = SliderDefaults.trackSprite(),
    fillSprite: SokitsuSprite = SliderDefaults.fillSprite(),
    interactionSource: MutableInteractionSource? = null,
) {
    NumberSliderImpl(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        trackSprite = trackSprite,
        fillSprite = fillSprite,
        interactionSource = interactionSource,
        toSliderValue = { it },
        fromSliderValue = { it },
        label = { v ->
            valueToText?.invoke(v) ?: run {
                val span = valueRange.endInclusive - valueRange.start
                val percent = if (span > 0f) ((v - valueRange.start) / span * 100).roundToInt() else 0
                Text("$percent%")
            }
        },
    )
}

/**
 * [IntSlider] / [LongSlider] / [FloatSlider] / [DoubleSlider] / [DurationSlider] /
 * [PercentSlider] 的公共实现：只做数值 ↔ 进度换算与标签渲染，其余全部委托 [Slider]。
 *
 * @param toSliderValue 类型化取值 → Slider 浮点值（valueRange 坐标系）
 * @param fromSliderValue Slider 回调的浮点值 → 类型化取值
 *        （回调值已收敛进 valueRange 并按 steps 吸附）
 * @param label 数值标签，参数为当前值
 */
@Composable
private fun <T> NumberSliderImpl(
    value: T,
    onValueChange: ((T) -> Unit)?,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier,
    enabled: Boolean,
    onValueChangeFinished: (() -> Unit)?,
    colors: SliderColors,
    trackSprite: SokitsuSprite,
    fillSprite: SokitsuSprite,
    interactionSource: MutableInteractionSource?,
    toSliderValue: (T) -> Float,
    fromSliderValue: (Float) -> T,
    label: @Composable (T) -> Unit,
) {
    Slider(
        value = toSliderValue(value),
        onValueChange = onValueChange?.let { change ->
            { sliderValue: Float -> change(fromSliderValue(sliderValue)) }
        },
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        trackSprite = trackSprite,
        fillSprite = fillSprite,
        label = { label(value) },
        interactionSource = interactionSource,
    )
}
