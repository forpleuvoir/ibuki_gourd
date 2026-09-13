package moe.forpleuvoir.ibukigourd.ui.sokitsu.draw

import com.mojang.blaze3d.pipeline.RenderPipeline
import moe.forpleuvoir.ibukigourd.render.extension.AnchorPosition
import moe.forpleuvoir.compose_minecraft.platform.render.CustomDrawContext
import moe.forpleuvoir.compose_minecraft.platform.render.MinecraftRenderPlugin
import moe.forpleuvoir.compose_minecraft.platform.render.toMatrix3x2f
import moe.forpleuvoir.compose_minecraft.platform.render.toScreenRectangle
import moe.forpleuvoir.ibukigourd.render.IGRenderPipelines
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.CenterFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureTintMode
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.SLOT_NONE
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.SLOT_TONE
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasManager
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuLayerSprite
import moe.forpleuvoir.ibukigourd.util.identifier
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.BlitRenderState
import net.minecraft.client.renderer.state.gui.TiledBlitRenderState
import net.minecraft.resources.Identifier
import org.joml.Matrix3x2f
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Sokitsu 精灵渲染插件：消费 [SokitsuSpriteDrawData]，逐图层提交
 * [BlitRenderState]（stretch / 九宫格）或 [TiledBlitRenderState]（tile）。
 *
 * 图层着色两维正交：**合成策略**（[SokitsuLayerSprite.tintMode]）决定管线 ——
 * Mask 走 [IGRenderPipelines.SOKITSU_TINT_MASK]（纯色替换），Multiply / Passthrough 走
 * [RenderPipelines.GUI_TEXTURED]（顶点色 × 纹理）；**取哪个色**由 [SokitsuLayerSprite.colorSlot]
 * 解析，见 [resolveSlotColor]。
 *
 * 阴影：[SokitsuLayerSprite.isShadow] 图层（layerId == "shadow"）先于普通图层绘制，
 * 目标矩形向光源反方向偏移（[SokitsuSpriteDrawData.shadowOffset]），外观由素材定义。
 *
 * 图层级不透明度：[CustomDrawContext.alpha]（由 `Modifier.alpha` / `graphicsLayer { alpha }`
 * 在回放阶段烘焙进命令 paint）乘进每个图层的调制色，因此整层 alpha 对精灵生效。
 */
object SokitsuSpritePlugin : MinecraftRenderPlugin {

    val TAG: Identifier = identifier("sokitsu_sprite")

    override fun onDraw(tag: Identifier, data: Any?, context: CustomDrawContext): Boolean {
        if (tag != TAG) return false
        val drawData = data as? SokitsuSpriteDrawData ?: return false
        drawSprite(drawData, context)
        return true
    }

    private fun drawSprite(data: SokitsuSpriteDrawData, context: CustomDrawContext) {
        val sprite = data.sprite
        if (sprite.isEmpty) return
        val w = data.size.width
        val h = data.size.height
        if (w <= 0 || h <= 0) return
        val atlas = SokitsuAtlasManager.atlasTexture(sprite.atlasLocation) ?: return

        val textureSetup = TextureSetup.singleTexture(atlas.getTextureView(), atlas.getSampler())
        val pose = context.matrix.toMatrix3x2f()
        val scissor = context.scissor?.toScreenRectangle()
        // 图层级不透明度：无图层/未设置 alpha 时为 1f
        val alpha = context.alpha

        // 阴影图层（layerId == "shadow"）先画：向光源反方向偏移，外观由素材定义

        sprite.layers.forEachIndexed { index, layer ->
            if (layer.isShadow)
                emitLayer(
                    context, layer, data, index, textureSetup, pose, scissor, alpha,
                    destOffsetX = data.shadowOffset.x, destOffsetY = data.shadowOffset.y
                )
            else
                emitLayer(context, layer, data, index, textureSetup, pose, scissor, alpha)
        }
    }

    /**
     * 提交单个图层（三个 fill 模式分发）。
     *
     * [destOffsetX]/[destOffsetY] 目标矩形偏移（阴影图层用）；
     * [alpha] 图层级不透明度（见 [CustomDrawContext.alpha]），乘进该图层调制色。
     */
    private fun emitLayer(
        context: CustomDrawContext,
        layer: SokitsuLayerSprite,
        data: SokitsuSpriteDrawData,
        index: Int,
        textureSetup: TextureSetup,
        pose: Matrix3x2f,
        scissor: ScreenRectangle?,
        alpha: Float,
        destOffsetX: Int = 0,
        destOffsetY: Int = 0,
    ) {
        val w = data.size.width
        val h = data.size.height
        val color = scaleAlpha(data.tintColors.getOrNull(index) ?: return, alpha)
        val pipeline = sokitsuLayerPipeline(layer)
        when (val fill = layer.fill) {
            is TextureFill.Stretch   -> context.sink.addElement(
                BlitRenderState(
                    pipeline, textureSetup, pose,
                    destOffsetX, destOffsetY, destOffsetX + w, destOffsetY + h,
                    layer.u0, layer.u1, layer.v0, layer.v1,
                    color, scissor,
                )
            )

            is TextureFill.Tile      -> {
                val tile = tileSizePx(layer.logicalWidth, fill.scale, data.pixelScale)
                    .roundToInt().coerceAtLeast(1)
                context.sink.addElement(
                    TiledBlitRenderState(
                        pipeline, textureSetup, pose,
                        tile, tile,
                        destOffsetX, destOffsetY, destOffsetX + w, destOffsetY + h,
                        layer.u0, layer.u1, layer.v0, layer.v1,
                        color, scissor,
                    )
                )
            }

            is TextureFill.NinePatch -> drawNinePatch(
                layer, fill, w, h, data.pixelScale, color, textureSetup, pose, scissor, context,
                destOffsetX, destOffsetY, pipeline,
            )
        }
    }

    /**
     * 提交九宫格图层：按 [TextureFill.NinePatch.border] + [SokitsuLayerSprite.disabledSlices] 切片。
     *
     * 八个边角分片拉伸整片源图；中心格按 [TextureFill.NinePatch.centerFill] 分流：
     * [CenterFill.Stretch] 拉伸整片，[CenterFill.Tile] 按中心格源图平铺（单元尺寸见
     * [ninePatchCenterTileSizePx]，末个不满单元由 `TiledBlitRenderState` 按源 UV 截断）。
     */
    private fun drawNinePatch(
        layer: SokitsuLayerSprite,
        fill: TextureFill.NinePatch,
        w: Int,
        h: Int,
        pixelScale: Int,
        color: Int,
        textureSetup: TextureSetup,
        pose: Matrix3x2f,
        scissor: ScreenRectangle?,
        context: CustomDrawContext,
        destOffsetX: Int = 0,
        destOffsetY: Int = 0,
        pipelineOverride: RenderPipeline? = null,
    ) {
        val pipeline = pipelineOverride ?: sokitsuLayerPipeline(layer)
        val border = fill.border
        // 纹素映射倍率：1 素材像素 = pixelScale / 素材密度 个屏幕像素（负值保留，外扩语义）
        val scale = pixelScale.toFloat() / layer.density
        val left = border.left * scale
        val top = border.top * scale
        val right = border.right * scale
        val bottom = border.bottom * scale

        // 目标边界（屏幕像素）吸附到整数，保证相邻分片无缝拼接
        val xs = ninePatchBoundaries(left, right, w.toFloat())
        val ys = ninePatchBoundaries(top, bottom, h.toFloat())
        val xi = IntArray(4) { xs[it].roundToInt() }
        val yi = IntArray(4) { ys[it].roundToInt() }

        // 源 UV 边界（素材物理像素）：取 border 绝对值 —— 目标侧负值负责向外扩，采样侧必须
        // 落在纹理内（取精灵最外圈真实像素），否则 getU/getV 线性外推进 padding 采到透明，
        // 外扩段虽在正确位置却不可见（负 border 表现为"被无视"）
        val srcW = layer.width.toFloat()
        val srcH = layer.height.toFloat()
        val su = ninePatchBoundaries(abs(border.left).toFloat(), abs(border.right).toFloat(), srcW)
        val sv = ninePatchBoundaries(abs(border.top).toFloat(), abs(border.bottom).toFloat(), srcH)

        // 中心格平铺：tile 单元取中心格源尺寸（su/sv 中段），与运行时可变的目标中心区域无关
        val centerTile = fill.centerFill == CenterFill.Tile
        val centerTileSize =
            ninePatchCenterTileSizePx(su[2] - su[1], sv[2] - sv[1], fill.centerScale, pixelScale, layer.density)
        val centerTileW = centerTileSize[0].roundToInt().coerceAtLeast(1)
        val centerTileH = centerTileSize[1].roundToInt().coerceAtLeast(1)

        fun emitCell(col: Int, row: Int, x: Int, y: Int, cw: Int, ch: Int) {
            if (cw <= 0 || ch <= 0) return
            val u0 = layer.getU(su[col] / srcW)
            val u1 = layer.getU(su[col + 1] / srcW)
            val v0 = layer.getV(sv[row] / srcH)
            val v1 = layer.getV(sv[row + 1] / srcH)
            if (centerTile && col == 1 && row == 1) {
                context.sink.addElement(
                    TiledBlitRenderState(
                        pipeline, textureSetup, pose,
                        centerTileW, centerTileH,
                        x + destOffsetX, y + destOffsetY, x + destOffsetX + cw, y + destOffsetY + ch,
                        u0, u1, v0, v1,
                        color, scissor,
                    )
                )
                return
            }
            emitSokitsuBlit(
                pipeline, textureSetup, pose,
                x + destOffsetX, y + destOffsetY, cw, ch,
                u0, u1, v0, v1, color, scissor, context,
            )
        }

        for (row in 0..2) {
            for (col in 0..2) {
                val index = row * 3 + col
                if (index in layer.disabledSlices) continue
                val cx = xi[col]
                val cw = xi[col + 1] - xi[col]
                val cy = yi[row]
                val ch = yi[row + 1] - yi[row]
                if (cw <= 0 || ch <= 0) continue
                emitCell(col, row, cx, cy, cw, ch)
            }
        }
    }

}

/**
 * 图层的渲染管线，由 **合成策略** [SokitsuLayerSprite.tintMode] 决定（与取哪个色的 colorSlot 无关）：
 * - [TextureTintMode.Mask]：Mask 管线（纯顶点色替换，忽略纹理 RGB，只留 alpha）
 * - [TextureTintMode.Multiply] / [TextureTintMode.Passthrough]：原版贴图管线（顶点色 × 纹理）；
 *   Passthrough 的顶点色由 [resolveSlotColor] 取白，故输出纹理原样
 */
internal fun sokitsuLayerPipeline(layer: SokitsuLayerSprite): RenderPipeline =
    when (layer.tintMode) {
        TextureTintMode.Mask -> IGRenderPipelines.SOKITSU_TINT_MASK
        else                 -> RenderPipelines.GUI_TEXTURED
    }

/**
 * 提交单条 blit（目标矩形 + 源 UV + 调制色）：宽或高非正时跳过。
 */
internal fun emitSokitsuBlit(
    pipeline: RenderPipeline, textureSetup: TextureSetup, pose: Matrix3x2f,
    x: Int, y: Int, w: Int, h: Int,
    u0: Float, u1: Float, v0: Float, v1: Float,
    color: Int, scissor: ScreenRectangle?,
    context: CustomDrawContext,
) {
    if (w <= 0 || h <= 0) return
    context.sink.addElement(
        BlitRenderState(
            pipeline, textureSetup, pose,
            x, y, x + w, y + h,
            u0, u1, v0, v1,
            color, scissor,
        )
    )
}

/**
 * 把图层级不透明度 [alpha] 乘进 0xAARRGGBB 调制色的 alpha 通道。
 *
 * [alpha] 为 1f（无 `Modifier.alpha` / `graphicsLayer` 图层，或运行时未携带 paint）时原样返回，
 * 避免无谓的位运算；结果 alpha 四舍五入取整，0 表示该图层整层不可见（仍会提交命令）。
 */
internal fun scaleAlpha(argb: Int, alpha: Float): Int {
    if (alpha >= 1f) return argb
    val a = ((argb ushr 24 and 0xFF) * alpha).roundToInt().coerceIn(0, 255)
    return (argb and 0x00FFFFFF) or (a shl 24)
}
