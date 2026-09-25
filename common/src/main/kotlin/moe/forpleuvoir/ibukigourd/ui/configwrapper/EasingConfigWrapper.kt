package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurveEditor
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurvePlotDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlexibleDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip.tooltip
import moe.forpleuvoir.ibukigourd.util.math.easing.CubicBezier
import moe.forpleuvoir.ibukigourd.util.math.easing.EasingPreset
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.item.ConfigEnum

/**
 * 缓动设置行：**一行** [EasingPreset] 选择器，选中 [EasingPreset.Custom] 即弹出曲线编辑器，
 * 且此时悬停控件显示当前自定义曲线的预览。
 *
 * 交互与主题的配色方案行同构：内置项选中即生效，只有"自定义"需要开编辑器。自定义曲线是**另一份配置项**
 * （`easing_custom`，单独存储与序列化），页面上不另出一行 —— 入口挂在本行上，本行因此同时承担
 * "选预设"与"编辑自定义曲线"两件事。
 *
 * 编辑器与 [BezierCurveConfigWrapper] 一致：拿当前曲线的**副本**改，确认时才写回配置。
 *
 * @param config 缓动预设配置项
 * @param customConfig 自定义曲线配置项（预设为 [EasingPreset.Custom] 时生效）
 * @param modifier 作用于整行
 * @param yRange 编辑与预览画布的 y 轴显示区间
 */
@Composable
fun EasingConfigWrapper(
    config: ConfigEnum<EasingPreset>,
    customConfig: Config<CubicBezier>,
    modifier: Modifier = Modifier,
    yRange: ClosedFloatingPointRange<Float> = BezierCurvePlotDefaults.YRange,
) {
    val value by config.asState()
    val customCurve by customConfig.asState()
    var editing by remember(config) { mutableStateOf(false) }

    // 预览只对自定义曲线有意义：回弹 / 弹跳 / 弹性不是三次贝塞尔，没有四点可画
    val preview = if (value == EasingPreset.Custom) {
        Modifier.tooltip(delay = ConfigRowDefaults.TooltipDelay) {
            CurveTooltipPreview(customCurve, yRange)
        }
    } else {
        Modifier
    }

    EnumConfigWrapper(
        config = config,
        modifier = modifier,
        controlModifier = preview,
        onSelected = { selected -> if (selected == EasingPreset.Custom) editing = true },
    )

    if (editing) {
        // 打开时复制当前曲线：编辑只改副本，确认时才写回配置，关闭即丢弃
        var draft by remember { mutableStateOf(customCurve) }
        FlexibleDialog(
            onDismissRequest = { editing = false },
            onConfirmRequest = {
                customConfig.setValue(draft)
                true
            },
            title = { ConfigDialogTitle(customConfig) },
            content = {
                BezierCurveEditor(
                    value = draft,
                    onValueChange = { draft = it },
                    yRange = yRange,
                    modifier = Modifier.padding(ConfigControlDefaults.CurveDialogPadding),
                )
            },
        )
    }
}
