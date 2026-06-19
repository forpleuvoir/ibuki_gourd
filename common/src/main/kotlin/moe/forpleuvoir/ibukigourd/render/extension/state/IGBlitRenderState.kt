package moe.forpleuvoir.ibukigourd.render.extension.state

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import moe.forpleuvoir.ibukigourd.render.color
import moe.forpleuvoir.ibukigourd.render.uv
import moe.forpleuvoir.ibukigourd.render.vertex
import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import org.joml.Matrix3x2f

class IGBlitRenderState(
    private val x0: Float,
    private val y0: Float,
    private val x1: Float,
    private val y1: Float,
    private val u0: Float,
    private val v0: Float,
    private val u1: Float,
    private val v1: Float,
    private val color: Color,
    private val pose: Matrix3x2f,
    private val pipeline: RenderPipeline,
    private val textureSetup: TextureSetup,
    private val scissorArea: ScreenRectangle?,
    private val bounds: ScreenRectangle? = bounds(x0, y0, x1 - x0, y1 - y0, pose, scissorArea)
) : GuiElementRenderState {

    companion object {
        @JvmStatic
        internal fun bounds(x0: Float, y0: Float, width: Float, height: Float, pose: Matrix3x2f, scissorArea: ScreenRectangle?): ScreenRectangle? {
            val screenRectangle = ScreenRectangle(x0.toInt(), y0.toInt(), width.toInt(), height.toInt()).transformMaxBounds(pose)
            return if (scissorArea != null) scissorArea.intersection(screenRectangle) else screenRectangle
        }
    }

    override fun pipeline(): RenderPipeline = pipeline

    override fun textureSetup(): TextureSetup = textureSetup

    override fun scissorArea(): ScreenRectangle? = scissorArea

    override fun bounds(): ScreenRectangle? = bounds

    override fun buildVertices(consumer: VertexConsumer) {
        consumer.vertex(pose, x0, y0).uv(u0, v0).color(color)
        consumer.vertex(pose, x0, y1).uv(u0, v1).color(color)
        consumer.vertex(pose, x1, y1).uv(u1, v1).color(color)
        consumer.vertex(pose, x1, y0).uv(u1, v0).color(color)
    }


}