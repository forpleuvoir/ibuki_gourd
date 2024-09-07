package moe.forpleuvoir.ibukigourd.render

import moe.forpleuvoir.ibukigourd.render.shader.IGShaders.POSITION_HSV_COLOR
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats

object IGRenderLayers {

    val positionHsvColor: RenderLayer.MultiPhase = RenderLayer.of(
        "gui_position_hsv_color",
        VertexFormats.POSITION_COLOR,
        VertexFormat.DrawMode.QUADS,
        786432,
        RenderLayer.MultiPhaseParameters.builder().program(RenderPhase.ShaderProgram(POSITION_HSV_COLOR)).transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .depthTest(RenderPhase.LEQUAL_DEPTH_TEST).build(false)
    )

}