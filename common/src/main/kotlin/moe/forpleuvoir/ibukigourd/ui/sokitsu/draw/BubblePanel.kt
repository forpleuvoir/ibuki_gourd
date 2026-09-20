package moe.forpleuvoir.ibukigourd.ui.sokitsu.draw

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.render.extension.AnchorPosition
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale

/**
 * 气泡面板：九宫格气泡体 + 陷进气泡体边缘的九宫格箭头，内容经 [contentPadding] 内缩。
 *
 * 宽度与高度由 [content] 撑开，只受 [minSize] 下限约束 —— 不含任何 `fill`，因此不会铺满
 * 弹层的可用宽度；内容比面板小时在面板内**居中**摆放。
 *
 * 箭头主体伸在气泡体外、仅根部插进气泡缺口，故节点在箭头方向留出该箭头布局盒子的空隙
 * （见 [boxWidthPx] / [boxHeightPx]），否则伸出部分会被弹层裁剪。
 *
 * @param body 气泡体精灵（须为九宫格）
 * @param arrow 箭头精灵（须为九宫格；绘制区域 = 纹理尺寸 × 放大倍率，1:1 不缩放）
 * @param arrowAnchor 气泡相对目标的位置；箭头贴在**反向边**
 * @param arrowRatio 箭头沿所在边的相对位置（0 = 起端，1 = 终端，0.5 = 居中）
 * @param color 气泡体与箭头共用的染色色
 * @param minSize 气泡体最小尺寸下限
 * @param contentPadding 内容内边距
 */
@Composable
fun BubblePanel(
    body: SokitsuSprite,
    arrow: SokitsuSprite,
    arrowAnchor: AnchorPosition,
    modifier: Modifier = Modifier,
    arrowRatio: Float = 0.5f,
    color: Color = LocalSokitsuColor.current.takeOrElse { LocalColorScheme.current.surfaceVariant },
    minSize: DpSize = DpSize(0.dp, 0.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val pixelScale = LocalSokitsuPixelScale.current

    // 箭头从气泡体边缘探出到同侧：箭头方向预留 arrowOutsetPadding 的留白，避免被弹层裁剪；
    // 单层 Box，modifier 直接作用于气泡体节点（之前的 Box 套 Box 会让 modifier 落到外层留白盒上）。
    Box(
        modifier
            .padding(arrowOutsetPadding(arrowAnchor, arrow, pixelScale, density))
            .sokitsuBubbleSprite(
                body = body,
                arrow = arrow,
                arrowAnchor = arrowAnchor,
                arrowRatio = arrowRatio,
                color = color,
            )
            .sizeIn(minWidth = minSize.width, minHeight = minSize.height)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/**
 * 箭头伸出气泡外所需的单边留白 = 箭头布局盒子尺寸（箭头绘制区域 − 根部外扩量），
 * 即箭头露在气泡外的部分；加在箭头所在边的外侧（[AnchorPosition.Above] → 箭头在气泡底边 → 下方）。
 */
private fun arrowOutsetPadding(
    anchor: AnchorPosition,
    arrow: SokitsuSprite,
    pixelScale: Int,
    density: Density,
): PaddingValues {
    val px = when (anchor) {
        AnchorPosition.Above, AnchorPosition.Below -> arrow.boxHeightPx(pixelScale)
        AnchorPosition.Left, AnchorPosition.Right -> arrow.boxWidthPx(pixelScale)
    }
    val inset = with(density) { px.toDp() }
    return when (anchor) {
        AnchorPosition.Above -> PaddingValues(bottom = inset)
        AnchorPosition.Below -> PaddingValues(top = inset)
        AnchorPosition.Left -> PaddingValues(end = inset)
        AnchorPosition.Right -> PaddingValues(start = inset)
    }
}
