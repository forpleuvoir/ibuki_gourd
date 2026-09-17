package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.fromToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve

/**
 * 横向进度条：**轨道 + 已完成填充**两个纯色矩形，用于表达确定的进度（缓存加载、任务完成度等）。
 *
 * 不依赖任何纹理素材——像素风里进度条是实心色块，画成精灵反而增加图集与资源包维护成本。
 * 颜色走"调用点传参 > 组件 token（[ProgressTokens]）> 主题槽位"回退链。
 *
 * [progress] 会被钳制到 `0f..1f`；`0` 时不绘制填充（只留轨道），`1` 时填满。
 * 需要"只显示填充、不显示轨道"时把 [trackColor] 传 [Color.Transparent]。
 *
 * 宽度铺满可用空间（[fillMaxWidth]），故需放在有确定宽度的容器里（Column / Box）；
 * 需要固定宽度时在 [modifier] 里传 `Modifier.width(...)`。
 *
 * 本组件**不做动画**：进度值多由业务自己逐帧驱动（如缓存的已用 / 上限），
 * 需要平滑过渡时由调用方持有 `animateFloatAsState` 之类的状态再传进来。
 *
 * @param progress 进度，`0f..1f`（越界自动钳制）
 * @param color 填充色，未指定按 [ProgressTokens.Indicator] 解析
 * @param trackColor 轨道色，未指定按 [ProgressTokens.Track] 解析
 */
@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified,
) {
    val resolvedColor = color.resolve(ProgressTokens.Indicator)
    val resolvedTrack = trackColor.takeOrElse {
        LocalColorScheme.current.fromToken(ProgressTokens.Track)
    }
    val fraction = progress.coerceIn(0f, 1f)

    Box(
        modifier
            .fillMaxWidth()
            .height(ProgressDefaults.height)
            .drawBehind {
                if (size.width <= 0f || size.height <= 0f) return@drawBehind
                drawRect(
                    color = resolvedTrack,
                    topLeft = Offset.Zero,
                    size = size,
                )
                val filled = size.width * fraction
                if (filled <= 0f) return@drawBehind
                drawRect(
                    color = resolvedColor,
                    topLeft = Offset.Zero,
                    size = Size(filled, size.height),
                )
            },
    )
}
