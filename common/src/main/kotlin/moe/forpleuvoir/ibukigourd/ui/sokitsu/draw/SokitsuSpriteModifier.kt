package moe.forpleuvoir.ibukigourd.ui.sokitsu.draw

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.recordCustomDraw
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.compose_minecraft.platform.ui.LocalShadowLight
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorTone
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuTone
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.takeOrElse
import kotlin.math.roundToInt

/**
 * 在背景绘制一个 Sokitsu 精灵（与 [Modifier.background] 同模式 —— 节点参与 DrawScope 管道，
 * 记录一条自定义绘制命令，由 [SokitsuSpritePlugin] 在渲染阶段提交）。
 *
 * 精灵按所选色板（[tone]）对各图层染色：图层 colorLevel 决定色阶、tintMode 决定着色模式；
 * 渲染像素放大倍率取 [LocalSokitsuPixelScale]。
 *
 * [tone] 默认 = [LocalSokitsuTone].current，未指定时回退 [LocalColorScheme].current.primary。
 * 调用方可：
 * - 显式传 [tone] 覆盖作用域默认；
 * - 或在外层用 [androidx.compose.runtime.CompositionLocalProvider] 整体切换为 secondary / error / 自定义。
 *
 * 阴影不是参数：精灵内 layerId 为 "shadow" 的图层（[SokitsuLayerSprite.isShadow]）自动按
 * 阴影渲染 —— 向 [LocalShadowLight] 光源反方向偏移（× 像素放大倍率）并先于普通图层绘制，
 * 外观完全由素材定义。
 */
@Stable
@Composable
fun Modifier.sokitsuSprite(
    sprite: SokitsuSprite,
    tone: ColorTone = LocalSokitsuTone.current.takeOrElse { LocalColorScheme.current.primary },
): Modifier = this.then(SokitsuSpriteElement(sprite, tone))

private class SokitsuSpriteElement(
    private val sprite: SokitsuSprite,
    private val tone: ColorTone,
) : ModifierNodeElement<SokitsuSpriteNode>() {

    override fun create(): SokitsuSpriteNode = SokitsuSpriteNode(sprite, tone)

    override fun update(node: SokitsuSpriteNode) {
        node.sprite = sprite
        node.tone = tone
        node.invalidateDraw()
    }

    override fun equals(other: Any?): Boolean =
        this === other || (other is SokitsuSpriteElement && sprite == other.sprite && tone == other.tone)

    override fun hashCode(): Int = 31 * sprite.hashCode() + tone.hashCode()
}

private class SokitsuSpriteNode(
    var sprite: SokitsuSprite,
    var tone: ColorTone,
) : DrawModifierNode, Modifier.Node(), CompositionLocalConsumerModifierNode {

    override fun ContentDrawScope.draw() {
        val size = IntSize(this.size.width.roundToInt(), this.size.height.roundToInt())
        if (size.width > 0 && size.height > 0 && !sprite.isEmpty) {
            val pixelScale = currentValueOf(LocalSokitsuPixelScale)
            // 阴影图层偏移：向光源反方向（光 (-1,-1) → 影 (+1,+1)），乘像素放大倍率
            val light = currentValueOf(LocalShadowLight)
            val shadowOffset = IntOffset(
                (-light.x * pixelScale).roundToInt(),
                (-light.y * pixelScale).roundToInt(),
            )
            val data = buildSokitsuSpriteDrawData(sprite, size, pixelScale, tone, shadowOffset)
            drawContext.canvas.recordCustomDraw(
                tag = SokitsuSpritePlugin.TAG,
                data = data,
                paint = null,
                layer3D = null,
            )
        }
        drawContent()
    }
}
