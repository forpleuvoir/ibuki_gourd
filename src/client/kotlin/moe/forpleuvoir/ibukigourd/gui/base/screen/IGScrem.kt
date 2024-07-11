package moe.forpleuvoir.ibukigourd.gui.base.screen

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget

interface IGScreen : DrawableElementContainer {

    val transform: Transform

    var focusedWidget: IGWidget?

    /**
     * GUI层
     */
    val layers: List<GuiLayer>

}