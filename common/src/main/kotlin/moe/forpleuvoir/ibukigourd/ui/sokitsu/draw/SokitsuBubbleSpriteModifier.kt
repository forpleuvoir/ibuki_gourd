package moe.forpleuvoir.ibukigourd.ui.sokitsu.draw

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.recordCustomDraw
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import moe.forpleuvoir.compose_minecraft.platform.ui.LocalShadowLight
import moe.forpleuvoir.compose_minecraft.platform.ui.draw.buildPaint
import moe.forpleuvoir.ibukigourd.render.extension.AnchorPosition
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import kotlin.math.roundToInt

/**
 * 在背景绘制一个气泡：九宫格气泡体 + 陷进气泡体边缘的九宫格箭头（与 [sokitsuSprite] 同模式 ——
 * 记录一条自定义绘制命令，由 [SokitsuBubbleSpritePlugin] 在渲染阶段提交）。
 *
 * **节点尺寸即气泡体尺寸**：箭头陷进气泡体内，不使气泡向外扩展。
 *
 * @param body 气泡体精灵（须为 [TextureFill.NinePatch]）
 * @param arrow 箭头精灵（须为 [TextureFill.NinePatch]；绘制区域 = 纹理尺寸 × 放大倍率，1:1 不缩放）
 * @param arrowAnchor 气泡相对目标的位置；箭头贴在**反向边**（[AnchorPosition.Above] → 气泡底边），
 *   该边中格按箭头绘制区域断开
 * @param arrowRatio 箭头沿所在边的相对位置（0 = 起端，1 = 终端，0.5 = 居中）；
 *   实际滑动范围按气泡体边框收窄，箭头不会压到圆角
 * @param color 气泡体与箭头共用的染色色（默认 surfaceVariant，未指定按 [LocalSokitsuColor] 作用域回落）
 */
@Composable
fun Modifier.sokitsuBubbleSprite(
    body: SokitsuSprite,
    arrow: SokitsuSprite,
    arrowAnchor: AnchorPosition,
    arrowRatio: Float = 0.5f,
    color: Color = LocalSokitsuColor.current.takeOrElse { LocalColorScheme.current.surfaceVariant },
): Modifier {
    requireNinePatch(body, "body")
    requireNinePatch(arrow, "arrow")
    return this.then(SokitsuBubbleSpriteElement(body, arrow, arrowAnchor, arrowRatio, color))
}

/**
 * 气泡的两个精灵必须是九宫格：箭头依赖负 border 探进气泡、气泡体依赖中格断开。
 * 非九宫格图层直接 [error]，不做静默降级。空的（未加载）精灵不检查。
 */
private fun requireNinePatch(sprite: SokitsuSprite, role: String) {
    sprite.layers.firstOrNull { it.fill !is TextureFill.NinePatch }?.let {
        error(
            "sokitsuBubbleSprite $role '${sprite.textureId}' layer '${it.layerId}' " +
                "requires nine-patch fill, but was ${it.fill}"
        )
    }
}

private class SokitsuBubbleSpriteElement(
    private val body: SokitsuSprite,
    private val arrow: SokitsuSprite,
    private val arrowAnchor: AnchorPosition,
    private val arrowRatio: Float,
    private val color: Color,
) : ModifierNodeElement<SokitsuBubbleSpriteNode>() {

    override fun create(): SokitsuBubbleSpriteNode =
        SokitsuBubbleSpriteNode(body, arrow, arrowAnchor, arrowRatio, color)

    override fun update(node: SokitsuBubbleSpriteNode) {
        node.body = body
        node.arrow = arrow
        node.arrowAnchor = arrowAnchor
        node.arrowRatio = arrowRatio
        node.color = color
        node.invalidateDraw()
    }

    override fun equals(other: Any?): Boolean =
        this === other || (
            other is SokitsuBubbleSpriteElement &&
                body == other.body &&
                arrow == other.arrow &&
                arrowAnchor == other.arrowAnchor &&
                arrowRatio == other.arrowRatio &&
                color == other.color
            )

    override fun hashCode(): Int {
        var result = body.hashCode()
        result = 31 * result + arrow.hashCode()
        result = 31 * result + arrowAnchor.hashCode()
        result = 31 * result + arrowRatio.hashCode()
        result = 31 * result + color.hashCode()
        return result
    }
}

private class SokitsuBubbleSpriteNode(
    var body: SokitsuSprite,
    var arrow: SokitsuSprite,
    var arrowAnchor: AnchorPosition,
    var arrowRatio: Float,
    var color: Color,
) : DrawModifierNode, Modifier.Node(), CompositionLocalConsumerModifierNode {

    override fun ContentDrawScope.draw() {
        val size = IntSize(this.size.width.roundToInt(), this.size.height.roundToInt())
        if (size.width > 0 && size.height > 0 && !body.isEmpty) {
            val pixelScale = currentValueOf(LocalSokitsuPixelScale)
            val light = currentValueOf(LocalShadowLight)
            val shadowOffset = IntOffset(
                (-light.x * pixelScale).roundToInt(),
                (-light.y * pixelScale).roundToInt(),
            )
            val data = SokitsuBubbleSpriteDrawData(
                body = body,
                arrow = arrow,
                size = size,
                pixelScale = pixelScale,
                arrowAnchor = arrowAnchor,
                arrowRatio = arrowRatio,
                bodyTintColors = body.layers.map {
                    resolveSlotColor(it.colorSlot, it.tintMode, color, currentValueOf(LocalColorScheme)).toArgb()
                },
                arrowTintColors = arrow.layers.map {
                    resolveSlotColor(it.colorSlot, it.tintMode, color, currentValueOf(LocalColorScheme)).toArgb()
                },
                shadowOffset = shadowOffset,
            )
            drawContext.canvas.recordCustomDraw(
                tag = SokitsuBubbleSpritePlugin.TAG,
                data = data,
                paint = buildPaint(Color.White),
                layer3D = null,
            )
        }
        drawContent()
    }
}
