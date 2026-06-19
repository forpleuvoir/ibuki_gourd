package moe.forpleuvoir.ibukigourd.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import moe.forpleuvoir.ibukigourd.mixin.client.RenderPipelinesAccessor
import moe.forpleuvoir.ibukigourd.util.identifier

object IGRenderPipelines {

    private val GUI_HSV_COLOR_SNIPPET: RenderPipeline.Snippet = RenderPipeline.builder()
        .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
        .withUniform("Projection", UniformType.UNIFORM_BUFFER)
        .withVertexShader("core/gui")
        .withFragmentShader(identifier("core/position_hsv_color"))
        .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
        .buildSnippet()


    val GUI_HSV_COLOR: RenderPipeline = RenderPipelinesAccessor.register(
        RenderPipeline.builder(GUI_HSV_COLOR_SNIPPET)
            .withLocation(identifier("pipeline/gui_hsv_color"))
            .build()
    )


}