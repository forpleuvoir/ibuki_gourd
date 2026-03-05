package moe.forpleuvoir.ibukigourd.gui.base.render.state

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import moe.forpleuvoir.ibukigourd.gui.base.render.state.IGBlitRenderState.Companion.bounds
import moe.forpleuvoir.ibukigourd.render.color
import moe.forpleuvoir.ibukigourd.render.uv
import moe.forpleuvoir.ibukigourd.render.vertex
import moe.forpleuvoir.nebula.common.color.ARGBColor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.gui.render.state.GuiElementRenderState
import net.minecraft.util.Mth
import org.joml.Matrix3x2f

data class IGTiledBlitRenderState(
    private val x0: Float,
    private val y0: Float,
    private val x1: Float,
    private val y1: Float,
    private val u0: Float,
    private val v0: Float,
    private val u1: Float,
    private val v1: Float,
    private val tileWidth: Float,
    private val tileHeight: Float,
    private val color: ARGBColor,
    private val pose: Matrix3x2f,
    private val pipeline: RenderPipeline,
    private val textureSetup: TextureSetup,
    private val scissorArea: ScreenRectangle?,
    private val bounds: ScreenRectangle? = bounds(x0, y0, x1 - x0, y1 - y0, pose, scissorArea)
) : GuiElementRenderState {

    override fun pipeline(): RenderPipeline = pipeline

    override fun textureSetup(): TextureSetup = textureSetup

    override fun scissorArea(): ScreenRectangle? = scissorArea

    override fun bounds(): ScreenRectangle? = bounds

    override fun buildVertices(consumer: VertexConsumer, z: Float) {
        val width = x1 - x0;
        val height = y1 - y0

        var usedWidth = 0f
        while (usedWidth < width) {
            val remainingWidth = width - usedWidth
            val (currentWidth, currentU) = if (tileWidth <= remainingWidth) {
                tileWidth to u1
            } else {
                remainingWidth to Mth.lerp(remainingWidth / tileWidth, u0, u1)
            }

            var usedHeight = 0f
            while (usedHeight < height) {
                val remainingHeight = height - usedHeight
                val (currentHeight, currentV) = if (tileHeight <= remainingHeight) {
                    tileHeight to v1
                } else {
                    remainingHeight to Mth.lerp(remainingHeight / tileHeight, v0, v1)
                }

                val cX0 = x0 + usedWidth
                val cX1 = x0 + usedWidth + currentWidth
                val cY0 = y0 + usedHeight
                val cY1 = y0 + usedHeight + currentHeight

                consumer.vertex(pose, cX0, cY0, z).uv(u0, v0).color(color)
                consumer.vertex(pose, cX0, cY1, z).uv(u0, currentV).color(color)
                consumer.vertex(pose, cX1, cY1, z).uv(currentU, currentV).color(color)
                consumer.vertex(pose, cX1, cY0, z).uv(currentU, v0).color(color)

                usedHeight += tileHeight
            }
            usedWidth += tileWidth
        }

    }

}