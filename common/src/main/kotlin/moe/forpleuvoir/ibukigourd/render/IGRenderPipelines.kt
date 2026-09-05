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

    // Sokitsu 精灵着色管线：共用一个 sokitsu_tint.fsh，经 withShaderDefine 切出三变体。
    private val SOKITSU_TINT_SNIPPET: RenderPipeline.Snippet = RenderPipeline.builder()
        .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
        .withVertexShader("core/position_tex_color")
        .withFragmentShader(identifier("core/sokitsu_tint"))
        .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
        .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .buildSnippet()

    val SOKITSU_TINT_MASK: RenderPipeline = RenderPipeline.builder(SOKITSU_TINT_SNIPPET)
        .withLocation(identifier("pipeline/sokitsu_tint_mask"))
        .withShaderDefine("SOKITSU_MASK")
        .build()

    val SOKITSU_TINT: RenderPipeline = RenderPipeline.builder(SOKITSU_TINT_SNIPPET)
        .withLocation(identifier("pipeline/sokitsu_tint"))
        .withShaderDefine("SOKITSU_TINT")
        .build()

    val SOKITSU_TINT_HUE_SHIFT: RenderPipeline = RenderPipeline.builder(SOKITSU_TINT_SNIPPET)
        .withLocation(identifier("pipeline/sokitsu_tint_hue_shift"))
        .withShaderDefine("SOKITSU_HUESHIFT")
        .build()

}
