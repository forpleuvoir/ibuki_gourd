package moe.forpleuvoir.ibukigourd.gui.base.render.state

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import moe.forpleuvoir.ibukigourd.gui.base.render.state.IGBlitRenderState.Companion.bounds
import moe.forpleuvoir.ibukigourd.render.color
import moe.forpleuvoir.ibukigourd.render.vertex
import moe.forpleuvoir.nebula.common.color.ARGBColor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.gui.render.state.GuiElementRenderState
import org.joml.Matrix3x2f

data class ColoredBoxRenderState(
    private val x0: Float,
    private val y0: Float,
    private val x1: Float,
    private val y1: Float,
    /**
     * TOP LEFT
     */
    private val col1: ARGBColor,
    /**
     * BOTTOM LEFT
     */
    private val col2: ARGBColor,
    /**
     * BOTTOM RIGHT
     */
    private val col3: ARGBColor,
    /**
     * TOP RIGHT
     */
    private val col4: ARGBColor,
    private val pose: Matrix3x2f,
    private val pipeline: RenderPipeline,
    private val scissorArea: ScreenRectangle?,
    private val bounds: ScreenRectangle? = bounds(x0, y0, x1 - x0, y1 - y0, pose, scissorArea)
) : GuiElementRenderState {

    companion object {
        @JvmStatic
        fun horizontal(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            colorLeft: ARGBColor,
            colorRight: ARGBColor,
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
            colorTop: ARGBColor,
            colorBottom: ARGBColor,
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

    override fun buildVertices(consumer: VertexConsumer, z: Float) {
        consumer.vertex(pose, x0, y0, z).color(col1)
        consumer.vertex(pose, x0, y1, z).color(col2)
        consumer.vertex(pose, x1, y1, z).color(col3)
        consumer.vertex(pose, x1, y0, z).color(col4)
    }
}