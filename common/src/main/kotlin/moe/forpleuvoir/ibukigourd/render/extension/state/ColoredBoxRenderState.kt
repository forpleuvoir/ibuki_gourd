package moe.forpleuvoir.ibukigourd.render.extension.state

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import moe.forpleuvoir.ibukigourd.render.color
import moe.forpleuvoir.ibukigourd.render.vertex
import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import org.joml.Matrix3x2f

class ColoredBoxRenderState(
    private val x0: Float,
    private val y0: Float,
    private val x1: Float,
    private val y1: Float,
    /**
     * TOP LEFT
     */
    private val col1: Color,
    /**
     * BOTTOM LEFT
     */
    private val col2: Color,
    /**
     * BOTTOM RIGHT
     */
    private val col3: Color,
    /**
     * TOP RIGHT
     */
    private val col4: Color,
    private val pose: Matrix3x2f,
    private val pipeline: RenderPipeline,
    private val scissorArea: ScreenRectangle?,
    private val bounds: ScreenRectangle? = IGBlitRenderState.bounds(x0, y0, x1 - x0, y1 - y0, pose, scissorArea)
) : GuiElementRenderState {

    companion object {
        @JvmStatic
        fun horizontal(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            colorLeft: Color,
            colorRight: Color,
            pose: Matrix3x2f,
            pipeline: RenderPipeline,
            scissorArea: ScreenRectangle?
        ): ColoredBoxRenderState {
            return ColoredBoxRenderState(x0, y0, x1, y1, colorLeft, colorLeft, colorRight, colorRight, pose, pipeline, scissorArea)
        }

        @JvmStatic
        fun vertical(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            colorTop: Color,
            colorBottom: Color,
            pose: Matrix3x2f,
            pipeline: RenderPipeline,
            scissorArea: ScreenRectangle?
        ): ColoredBoxRenderState {
            return ColoredBoxRenderState(x0, y0, x1, y1, colorTop, colorBottom, colorBottom, colorTop, pose, pipeline, scissorArea)
        }
    }

    override fun pipeline(): RenderPipeline = pipeline

    override fun textureSetup(): TextureSetup = TextureSetup.noTexture()

    override fun scissorArea(): ScreenRectangle? = scissorArea

    override fun bounds(): ScreenRectangle? = bounds

    override fun buildVertices(consumer: VertexConsumer) {
        consumer.vertex(pose, x0, y0).color(col1)
        consumer.vertex(pose, x0, y1).color(col2)
        consumer.vertex(pose, x1, y1).color(col3)
        consumer.vertex(pose, x1, y0).color(col4)
    }
}