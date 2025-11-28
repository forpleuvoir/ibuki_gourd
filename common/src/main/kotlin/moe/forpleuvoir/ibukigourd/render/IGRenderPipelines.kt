package moe.forpleuvoir.ibukigourd.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import moe.forpleuvoir.ibukigourd.util.resourceLocation
import net.minecraft.client.renderer.RenderPipelines

object IGRenderPipelines {

    private val GUI_HSV_COLOR_SNIPPET: RenderPipeline.Snippet = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withVertexShader("core/gui")
        .withFragmentShader(resourceLocation("core/position_hsv_color"))
        .withBlend(BlendFunction.TRANSLUCENT)
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .buildSnippet()

    val GUI_HSV_COLOR: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(GUI_HSV_COLOR_SNIPPET)
            .withLocation(resourceLocation("pipeline/gui_hsv_color"))
            .build()
    )
}