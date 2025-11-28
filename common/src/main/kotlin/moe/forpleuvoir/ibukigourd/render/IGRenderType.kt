package moe.forpleuvoir.ibukigourd.render

import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.RenderType

object IGRenderType {

    val GUI: RenderType.CompositeRenderType = RenderType.create(
        "ig_gui",
        786432,
        RenderPipelines.GUI,
        RenderType.CompositeState.builder().createCompositeState(false)
    )

    val GUI_HSV_COLOR: RenderType.CompositeRenderType = RenderType.create(
        "ig_gui_hsv_color",
        786432,
        IGRenderPipelines.GUI_HSV_COLOR,
        RenderType.CompositeState.builder().createCompositeState(false)
    )

}