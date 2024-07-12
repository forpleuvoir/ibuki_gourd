package moe.forpleuvoir.ibukigourd.gui.base.screen

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.element.DrawableElementContainer
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer

interface IGScreen : DrawableElementContainer, WidgetContainer, IGWidget {

    //------------ IGScreen ------------\\

    var focusedWidget: IGWidget?

    var pauseGame: Boolean

    var closeOnEsc: Boolean

    var onClose: (() -> Unit)?

    var onDisplayed: (() -> Unit)?

    /**
     * GUI层
     */
    val layers: List<GuiLayer>


    //------------ Override ------------\\

    override var parentData: Any?

}