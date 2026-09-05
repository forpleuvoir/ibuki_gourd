package moe.forpleuvoir.ibukigourd.ui.sokitsu.draw

import com.mojang.blaze3d.pipeline.RenderPipeline
import moe.forpleuvoir.compose_minecraft.platform.render.CustomDrawContext
import moe.forpleuvoir.compose_minecraft.platform.render.MinecraftRenderPlugin
import moe.forpleuvoir.compose_minecraft.platform.render.toMatrix3x2f
import moe.forpleuvoir.compose_minecraft.platform.render.toScreenRectangle
import moe.forpleuvoir.ibukigourd.render.IGRenderPipelines
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureFill
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.TextureTintMode
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
import kotlin.math.roundToInt

/**
 * Sokitsu 精灵渲染插件：消费 [SokitsuSpriteDrawData]，逐图层提交
 * [BlitRenderState]（stretch / 九宫格）或 [TiledBlitRenderState]（tile）。
 *
 * 图层着色：colorLevel 非 null 走 [IGRenderPipelines] 的 sokitsu_tint 三变体管线；
 * colorLevel 为 null 的图层无主题染色，走原版 [RenderPipelines.GUI_TEXTURED] 原色直通。
 *
 * 阴影：[SokitsuLayerSprite.isShadow] 图层（layerId == "shadow"）先于普通图层绘制，
 * 目标矩形向光源反方向偏移（[SokitsuSpriteDrawData.shadowOffset]），外观由素材定义。
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

        // 阴影图层（layerId == "shadow"）先画：向光源反方向偏移，外观由素材定义

        sprite.layers.forEachIndexed { index, layer ->
            if (layer.isShadow)
                emitLayer(
                    context, layer, data, index, textureSetup, pose, scissor,
                    destOffsetX = data.shadowOffset.x, destOffsetY = data.shadowOffset.y
                )
            else
                emitLayer(context, layer, data, index, textureSetup, pose, scissor)
        }
    }

    /**
     * 提交单个图层（三个 fill 模式分发）。
     *
     * [destOffsetX]/[destOffsetY] 目标矩形偏移（阴影图层用）。
     */
    private fun emitLayer(
        context: CustomDrawContext,
        layer: SokitsuLayerSprite,
        data: SokitsuSpriteDrawData,
        index: Int,
        textureSetup: TextureSetup,
        pose: Matrix3x2f,
        scissor: ScreenRectangle?,
        destOffsetX: Int = 0,
        destOffsetY: Int = 0,
    ) {
        val w = data.size.width
        val h = data.size.height
        val color = data.tintColors.getOrNull(index) ?: return
        val pipeline = pipelineFor(layer)
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

    private fun pipelineFor(layer: SokitsuLayerSprite): RenderPipeline =
        if (layer.colorLevel == null) {
            RenderPipelines.GUI_TEXTURED
        } else {
            when (layer.tintMode) {
                TextureTintMode.Mask     -> IGRenderPipelines.SOKITSU_TINT_MASK
                TextureTintMode.Tint     -> IGRenderPipelines.SOKITSU_TINT
                TextureTintMode.HueShift -> IGRenderPipelines.SOKITSU_TINT_HUE_SHIFT
            }
        }

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
        val pipeline = pipelineOverride ?: pipelineFor(layer)
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

        // 源 UV 边界（素材物理像素）：负 border 向纹理外扩采样（越界由 getU/getV 线性外推进 padding）
        val srcW = layer.width.toFloat()
        val srcH = layer.height.toFloat()
        val su = ninePatchBoundaries(border.left.toFloat(), border.right.toFloat(), srcW)
        val sv = ninePatchBoundaries(border.top.toFloat(), border.bottom.toFloat(), srcH)

        for (row in 0..2) {
            for (col in 0..2) {
                val index = row * 3 + col
                if (index in layer.disabledSlices) continue
                val cx = xi[col]
                val cw = xi[col + 1] - xi[col]
                val cy = yi[row]
                val ch = yi[row + 1] - yi[row]
                if (cw <= 0 || ch <= 0) continue
                val u0 = layer.getU(su[col] / srcW)
                val u1 = layer.getU(su[col + 1] / srcW)
                val v0 = layer.getV(sv[row] / srcH)
                val v1 = layer.getV(sv[row + 1] / srcH)
                context.sink.addElement(
                    BlitRenderState(
                        pipeline, textureSetup, pose,
                        cx + destOffsetX, cy + destOffsetY,
                        cx + cw + destOffsetX, cy + ch + destOffsetY,
                        u0, u1, v0, v1,
                        color, scissor,
                    )
                )
            }
        }
    }
}
