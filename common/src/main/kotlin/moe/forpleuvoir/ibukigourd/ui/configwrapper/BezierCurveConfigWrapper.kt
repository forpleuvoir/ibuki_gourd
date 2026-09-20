package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurveEditor
import moe.forpleuvoir.ibukigourd.ui.curve.BezierCurvePlotDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlexibleDialog
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.util.math.easing.CubicBezier
import moe.forpleuvoir.nebula.config.Config
import java.util.Locale

/**
 * 缓动曲线：行内一块**可拖的速览画布**，右侧编辑按钮打开完整编辑器。
 *
 * 行内画布尺寸小（[ConfigControlDefaults.CurvePreviewSize]），但手势与完整编辑器一致
 * （命中式拾取、Alt 吸附、Shift 锁角），因此常用的微调不必开弹窗；要精确填数值或套预设时
 * 用右侧按钮打开 [BezierCurveEditor]（数值框 + 预设行）。
 *
 * 编辑**即时写回**配置（弹窗的确认 / 取消都只是关闭，不回滚）——需要恢复默认值时用行尾的重置按钮。
 *
 * @param config 曲线配置项
 * @param modifier 作用于整行
 * @param yRange y 轴显示区间（同时作为编辑器的显示区间）
 */
@Composable
fun BezierCurveConfigWrapper(
    config: Config<CubicBezier>,
    modifier: Modifier = Modifier,
    yRange: ClosedFloatingPointRange<Float> = BezierCurvePlotDefaults.YRange,
) {
    val value by config.asState()
    var editing by remember(config) { mutableStateOf(false) }

    ConfigRowWrapper(config, modifier) {
        // 行上不放画布（太小也看不清），只给控制点数值摘要；编辑一律进弹窗
        ConfigControlBlock(
            action = {
                IconButton(
                    onClick = { editing = true },
                    contentPadding = ConfigControlDefaults.IconButtonPadding,
                ) {
                    Icon(Icons.Edit, scale = configIconScale())
                }
            },
        ) {
            Surface(
                modifier = Modifier.weight(1f).height(configControlHeight()),
                contentAlignment = Alignment.Center,
            ) {
                Text(ConfigBezierSummary(value))
            }
        }
    }

    if (editing) {
        FlexibleDialog(
            onDismissRequest = { editing = false },
            onConfirmRequest = { true },
            title = { ConfigDialogTitle(config) },
            content = {
                BezierCurveEditor(
                    value = value,
                    onValueChange = { config.setValue(it) },
                    yRange = yRange,
                    modifier = Modifier.padding(ConfigControlDefaults.CurveDialogPadding),
                )
            },
        )
    }
}

/** 行上的控制点摘要：四个分量各两位小数（只说明"当前曲线长什么样"，编辑进弹窗）。 */
private fun ConfigBezierSummary(value: CubicBezier): String =
    "(%.2f, %.2f, %.2f, %.2f)".format(Locale.ROOT, value.x1, value.y1, value.x2, value.y2)
