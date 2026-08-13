package moe.forpleuvoir.ibukigourd.render

import com.mojang.blaze3d.PrimitiveTopology
import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import moe.forpleuvoir.ibukigourd.mixin.client.RenderPipelinesAccessor
import moe.forpleuvoir.ibukigourd.util.identifier
import net.minecraft.client.renderer.BindGroupLayouts

object IGRenderPipelines {

    private val GUI_HSV_COLOR_SNIPPET: RenderPipeline.Snippet = RenderPipeline.builder()
        .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
        .withVertexShader("core/gui")
        .withFragmentShader(identifier("core/position_hsv_color"))
        .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .buildSnippet()


    val GUI_HSV_COLOR: RenderPipeline = RenderPipelinesAccessor.register(
        RenderPipeline.builder(GUI_HSV_COLOR_SNIPPET)
            .withLocation(identifier("pipeline/gui_hsv_color"))
            .build()
    )


}
