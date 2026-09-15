package moe.forpleuvoir.ibukigourd.ui.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FloatField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IntField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import kotlin.math.roundToInt

/**
 * 颜色通道条：一条带渐变的横条 + 拖动指示条 + 右侧数值输入框。
 *
 * 不复用 sokitsu 的 `Slider`——那条是精灵驱动的（轨道 / 填充两张素材 + label 槽），没有指示块槽位，
 * 也没法把轨道画成透明，塞不进渐变底。这里整条**在同一个 [Canvas] 里画完**（棋盘 → 渐变 → 描边 → 指示条），
 * 不拆成多个叠层节点，避免各层尺寸 / 位置对不齐。
 *
 * 条高取 [ColorPickerDefaults.BarHeight]，**与右侧数值框的尺寸无关**（行高由数值框决定，条在其中居中）。
 *
 * 手势：按下即定位、随后拖动跟随，[integral] 时吸附整数；拖拽经 [DragInteraction] 手动上报
 * （裸 `pointerInput` 不会自动上报）。
 *
 * @param label 行首标签（单字符，如 H / S / V / R / G / B / A）
 * @param value 当前值（会被收敛到 [valueRange] 内用于绘制）
 * @param valueRange 取值区间
 * @param gradientColors 渐变色标（≥2 个）；alpha 条就是"同 RGB、不同 alpha"的两段色标
 * @param onValueChange 值变化回调
 * @param integral true = 数值框用 [IntField] 且拖动吸附整数（RGB / A 通道），false = [FloatField]
 * @param showCheckerboard 是否在渐变下方垫棋盘（alpha 条用）
 * @param suffix 数值框的**显示后缀**（如 `%`），只影响显示、不参与解析
 * @param valueToText 浮点通道的显示格式
 */
@Composable
fun ColorChannelSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    gradientColors: List<Color>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    integral: Boolean = false,
    showCheckerboard: Boolean = false,
    suffix: String? = null,
    valueToText: (Float) -> String = { "%.1f".format(it) },
) {
    val span = valueRange.endInclusive - valueRange.start
    val fraction = if (span > 0f) ((value - valueRange.start) / span).coerceIn(0f, 1f) else 0f

    // 手势协程里要读到最新回调，避免 pointerInput 因 lambda 身份变化而重启
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val interaction = remember { MutableInteractionSource() }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text(label, modifier = Modifier.width(ColorPickerDefaults.LabelWidth))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(ColorPickerDefaults.BarHeight)
                .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.NotAllowed)
                .pointerInput(enabled, valueRange.start, valueRange.endInclusive, integral) {
                    if (!enabled) return@pointerInput
                    awaitEachGesture {
                        // 按下即定位：不设触摸阈值，点哪跳哪
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val dragInteraction = DragInteraction.Start()
                        interaction.tryEmit(dragInteraction)
                        fun emitAt(x: Float) {
                            // pointerInput 作用域的 size 是 IntSize（像素），转 Float 再算比例
                            val width = size.width.toFloat().coerceAtLeast(1f)
                            val f = (x / width).coerceIn(0f, 1f)
                            val raw = valueRange.start + f * span
                            currentOnValueChange(if (integral) raw.roundToInt().toFloat() else raw)
                        }
                        try {
                            emitAt(down.position.x)
                            down.consume()
                            drag(down.id) { change ->
                                emitAt(change.position.x)
                                change.consume()
                            }
                            // drag() 对抬起与取消都静默返回，统一以 Stop 收尾
                            interaction.tryEmit(DragInteraction.Stop(dragInteraction))
                        } catch (e: Throwable) {
                            // 协程被取消或回调抛错时补发 Cancel，避免交互源残留未结束的 Start
                            interaction.tryEmit(DragInteraction.Cancel(dragInteraction))
                            throw e
                        }
                    }
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                if (showCheckerboard) {
                    drawCheckerboard(ColorPickerDefaults.BarTile)
                }
                if (gradientColors.size >= 2) {
                    drawRect(brush = Brush.horizontalGradient(gradientColors))
                }
                drawRect(color = ColorPickerDefaults.BarOutline, style = Stroke(width = 1f))
                // 指示条：比条本身略高（上下各冒出一截，读起来明确是"把手"），
                // 绘制节点不裁剪，所以冒出的部分直接画到条外即可；
                // 水平位置夹在条内，贴到两端时不会被折掉一半。
                val thumbWidth = ColorPickerDefaults.ThumbWidth.toPx()
                val thumbHeight = ColorPickerDefaults.ThumbHeight.toPx()
                val half = thumbWidth / 2f
                val x = (size.width * fraction).coerceIn(half, (size.width - half).coerceAtLeast(half))
                val top = (size.height - thumbHeight) / 2f
                drawRect(
                    color = ColorPickerDefaults.ThumbColor,
                    topLeft = Offset(x - half, top),
                    size = Size(thumbWidth, thumbHeight),
                )
                drawRect(
                    color = ColorPickerDefaults.ThumbOutline,
                    topLeft = Offset(x - half, top),
                    size = Size(thumbWidth, thumbHeight),
                    style = Stroke(width = 1f),
                )
            }
        }

        Spacer(Modifier.width(ColorPickerDefaults.FieldGap))

        if (integral) {
            IntField(
                value = value.roundToInt(),
                onValueChange = { currentOnValueChange(it.toFloat()) },
                valueRange = valueRange.start.roundToInt()..valueRange.endInclusive.roundToInt(),
                enabled = enabled,
                trailingIcon = suffix?.let { text -> { Text(text) } },
                modifier = Modifier.width(ColorPickerDefaults.FieldWidth),
            )
        } else {
            FloatField(
                value = value,
                onValueChange = { currentOnValueChange(it) },
                valueRange = valueRange,
                valueToText = valueToText,
                enabled = enabled,
                trailingIcon = suffix?.let { text -> { Text(text) } },
                modifier = Modifier.width(ColorPickerDefaults.FieldWidth),
            )
        }
    }
}
