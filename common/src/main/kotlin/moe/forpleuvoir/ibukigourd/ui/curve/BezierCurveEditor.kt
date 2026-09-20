package moe.forpleuvoir.ibukigourd.ui.curve

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FloatField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TextButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.ValueStep
import moe.forpleuvoir.ibukigourd.util.math.easing.CubicBezier
import java.util.Locale

/**
 * 预设曲线。
 *
 * [label] 是**曲线标识名**（与 CSS `cubic-bezier(...)` 的常用名对应），不是本地化文案；
 * 接进需要 i18n 的界面时由调用方自行提供列表与文案。
 */
data class BezierCurvePreset(
    /** 曲线标识名。 */
    val label: String,
    /** 该预设对应的曲线。 */
    val value: CubicBezier,
)

/**
 * 曲线编辑器：左侧 [BezierCurvePlot] 画布，右侧四个数值框（各控制点的 x / y），下方预设行。
 *
 * 数值框与拖拽共用同一份外部状态：x 限 `0f..1f`、y 限 [yRange]（与画布的拖动夹取一致）。
 * 数值框内部有 `lastSynced` 守卫，拖拽产生的值变化不会在正在输入时把文本顶掉。
 *
 * 画布尺寸由 [plotSize] 给定；数值框与预设行都在同一 [Column] 内，无需调用方额外排版。
 *
 * @param value 当前曲线（外部状态）
 * @param onValueChange 数值框 / 预设按钮 / 画布拖拽任一来源的改变回调
 * @param modifier 作用于最外层 [Column]
 * @param enabled 是否可交互（同时作用于画布、数值框与预设按钮）
 * @param yRange y 轴显示区间，同时作为 y 数值框的取值区间
 * @param snapStep 画布上按住 Alt 拖动时的吸附步长
 * @param snapWithAlt 是否启用画布 Alt 吸附
 * @param lockAngleWithShift 是否启用画布 Shift 锁角（控制点只沿自身锚点与当前位置的直线移动）
 * @param plotSize 画布边长
 * @param presets 预设行；传空列表则不渲染预设行
 */
@Composable
fun BezierCurveEditor(
    value: CubicBezier,
    onValueChange: (CubicBezier) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    yRange: ClosedFloatingPointRange<Float> = BezierCurvePlotDefaults.YRange,
    snapStep: Float = BezierCurvePlotDefaults.SnapStep,
    snapWithAlt: Boolean = BezierCurvePlotDefaults.SnapWithAlt,
    lockAngleWithShift: Boolean = BezierCurvePlotDefaults.LockAngleWithShift,
    plotSize: Dp = BezierCurveEditorDefaults.PlotSize,
    presets: List<BezierCurvePreset> = BezierCurveEditorDefaults.Presets,
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.Top) {
            BezierCurvePlot(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.size(plotSize),
                enabled = enabled,
                yRange = yRange,
                snapStep = snapStep,
                snapWithAlt = snapWithAlt,
                lockAngleWithShift = lockAngleWithShift,
            )

            Spacer(Modifier.width(BezierCurveEditorDefaults.ContentGap))

            Column(verticalArrangement = Arrangement.spacedBy(BezierCurveEditorDefaults.RowGap)) {
                ControlPointRow(
                    label = "P1",
                    x = value.x1,
                    y = value.y1,
                    yRange = yRange,
                    enabled = enabled,
                    onXChange = { onValueChange(value.copy(x1 = it)) },
                    onYChange = { onValueChange(value.copy(y1 = it)) },
                )
                ControlPointRow(
                    label = "P2",
                    x = value.x2,
                    y = value.y2,
                    yRange = yRange,
                    enabled = enabled,
                    onXChange = { onValueChange(value.copy(x2 = it)) },
                    onYChange = { onValueChange(value.copy(y2 = it)) },
                )
            }
        }

        if (presets.isNotEmpty()) {
            Spacer(Modifier.height(BezierCurveEditorDefaults.ContentGap))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(BezierCurveEditorDefaults.PresetGap),
                verticalArrangement = Arrangement.spacedBy(BezierCurveEditorDefaults.PresetGap),
            ) {
                presets.forEach { preset ->
                    TextButton(
                        onClick = { onValueChange(preset.value) },
                        text = preset.label,
                        enabled = enabled,
                    )
                }
            }
        }
    }
}

/**
 * 一个控制点的数值行：行首标签 + x 框 + y 框。
 *
 * @param label 行首标签（如 `P1`）
 * @param x 控制点 x（限 `0f..1f`）
 * @param y 控制点 y（限 [yRange]）
 * @param yRange y 的取值区间
 * @param enabled 是否可编辑
 * @param onXChange x 改变回调
 * @param onYChange y 改变回调
 */
@Composable
private fun ControlPointRow(
    label: String,
    x: Float,
    y: Float,
    yRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onXChange: (Float) -> Unit,
    onYChange: (Float) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BezierCurveEditorDefaults.RowGap),
    ) {
        Text(label, modifier = Modifier.width(BezierCurveEditorDefaults.LabelWidth))
        FloatField(
            value = x,
            onValueChange = onXChange,
            valueRange = 0f..1f,
            valueToText = BezierCurveEditorDefaults.ValueToText,
            valueStep = BezierCurveEditorDefaults.ValueStep,
            enabled = enabled,
            leadingIcon = { Text("x") },
            modifier = Modifier.width(BezierCurveEditorDefaults.FieldWidth),
        )
        FloatField(
            value = y,
            onValueChange = onYChange,
            valueRange = yRange,
            valueToText = BezierCurveEditorDefaults.ValueToText,
            valueStep = BezierCurveEditorDefaults.ValueStep,
            enabled = enabled,
            leadingIcon = { Text("y") },
            modifier = Modifier.width(BezierCurveEditorDefaults.FieldWidth),
        )
    }
}

/**
 * 曲线编辑器的尺寸与预设缺省值。
 *
 * **不设 meta 段**：这里的量都是编辑器的排版常量（画布边长、间距、输入框宽度），
 * 不是随主题 / 资源包变化的量。
 */
object BezierCurveEditorDefaults {

    /** 画布边长。 */
    val PlotSize: Dp = 200.dp

    /** 画布与数值列之间、数值列与预设行之间的间距。 */
    val ContentGap: Dp = 16.dp

    /** 行间距（控制点行之间、行内标签与输入框之间）。 */
    val RowGap: Dp = 8.dp

    /** 控制点行首标签宽度（P1 / P2）。 */
    val LabelWidth: Dp = 24.dp

    /** 单个数值框宽度。 */
    val FieldWidth: Dp = 96.dp

    /** 预设按钮之间的间距。 */
    val PresetGap: Dp = 8.dp

    /** 数值框的显示格式：固定三位小数，且不随系统区域设置切换小数点符号（否则解析会失败）。 */
    val ValueToText: (Float) -> String = { "%.3f".format(Locale.ROOT, it) }

    /** 数值框的滚轮步进：基础 0.01，Shift 0.1 / Ctrl 0.2 / Alt 0.5。 */
    val ValueStep: ValueStep<Float> = ValueStep(0.01f, shift = 10, ctrl = 20, alt = 50)

    /** 预设行：CSS 常用曲线名 + 平台缺省曲线。 */
    val Presets: List<BezierCurvePreset> = listOf(
        BezierCurvePreset("linear", CubicBezier.Linear),
        BezierCurvePreset("standard", CubicBezier.Standard),
        BezierCurvePreset("ease-in", CubicBezier.EaseIn),
        BezierCurvePreset("ease-out", CubicBezier.EaseOut),
        BezierCurvePreset("ease-in-out", CubicBezier.EaseInOut),
    )
}
