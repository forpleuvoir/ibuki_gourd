package moe.forpleuvoir.ibukigourd.ui.sokitsu.draw

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.recordCustomDraw
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import moe.forpleuvoir.compose_minecraft.platform.ui.draw.buildPaint
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.compose_minecraft.platform.ui.LocalShadowLight
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuColor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import kotlin.math.roundToInt

/**
 * 在背景绘制一个 Sokitsu 精灵（与 [Modifier.background] 同模式 —— 节点参与 DrawScope 管道，
 * 记录一条自定义绘制命令，由 [SokitsuSpritePlugin] 在渲染阶段提交）。
 *
 * 精灵按 [color] 对各图层染色，两个维度正交：
 * - **合成策略**（图层的 `tintMode`）：`Mask` 走纯色替换管线、`Multiply` / `Passthrough` 走
 *   顶点色 × 纹理管线（`Passthrough` 顶点色取白，即原样输出）；
 * - **颜色来源**（图层的 `colorSlot`）：`tone` = [color]，`shadow` = 黑，`outline` = 描边色（可覆盖），
 *   其它 = [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorScheme] 同名词位，`none` = 白。
 *
 * 渲染像素放大倍率取 [LocalSokitsuPixelScale]。
 *
 * [color] 默认 = [LocalSokitsuColor].current，未指定时回退 [LocalColorScheme].current.primary。
 * 调用方可：
 * - 显式传 [color] 覆盖作用域默认；
 * - 或在外层用 [androidx.compose.runtime.CompositionLocalProvider] 整体切换为 secondary / error / 自定义。
 *
 * 阴影不是参数：`colorSlot` 为 "shadow" 的图层自动按阴影渲染 —— 向 [LocalShadowLight]
 * 光源反方向偏移（× 像素放大倍率）并先于普通图层绘制，外观完全由素材定义。
 *
 * 图层级不透明度（`Modifier.alpha` / `graphicsLayer { alpha }`）同样生效：命令携带 paint，
 * 回放时把图层 alpha 烘焙进 paint.alpha，由 [SokitsuSpritePlugin] 乘进各图层调制色。
 */
@Composable
fun Modifier.sokitsuSprite(
    sprite: SokitsuSprite,
    color: Color = LocalSokitsuColor.current.takeOrElse { LocalColorScheme.current.primary },
    outlineColor: Color = Color.Unspecified,
): Modifier = this.then(SokitsuSpriteElement(sprite, color, outlineColor))

private class SokitsuSpriteElement(
    private val sprite: SokitsuSprite,
    private val color: Color,
    private val outlineColor: Color,
) : ModifierNodeElement<SokitsuSpriteNode>() {

    override fun create(): SokitsuSpriteNode = SokitsuSpriteNode(sprite, color, outlineColor)

    override fun update(node: SokitsuSpriteNode) {
        node.sprite = sprite
        node.color = color
        node.outlineColor = outlineColor
        node.invalidateDraw()
    }

    override fun equals(other: Any?): Boolean =
        this === other || (other is SokitsuSpriteElement && sprite == other.sprite && color == other.color && outlineColor == other.outlineColor)

    override fun hashCode(): Int = 31 * (31 * (31 * sprite.hashCode() + color.hashCode()) + outlineColor.hashCode())
}

private class SokitsuSpriteNode(
    var sprite: SokitsuSprite,
    var color: Color,
    var outlineColor: Color,
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
            val scheme = currentValueOf(LocalColorScheme)
            val data = buildSokitsuSpriteDrawData(sprite, size, pixelScale, color, scheme, shadowOffset, outlineColor)
            // paint 必须存在：回放阶段（MinecraftCanvas.replayFrom 的 alphaMultiplier）把图层
            // alpha 烘焙进 paint.alpha，[SokitsuSpritePlugin] 再从 CustomDrawContext.alpha 取用；
            // 传 null 则这条命令没有 alpha 通道，Modifier.alpha 对该精灵失效。
            // color 只作占位（精灵调制色来自 data.tintColors，插件不读 paint.color）。
            drawContext.canvas.recordCustomDraw(
                tag = SokitsuSpritePlugin.TAG,
                data = data,
                paint = buildPaint(Color.White),
                layer3D = null,
            )
        }
        drawContent()
    }
}
