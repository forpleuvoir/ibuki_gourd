package moe.forpleuvoir.ibukigourd.gui.base.state

import moe.forpleuvoir.ibukigourd.gui.base.element.UserInteractionHandler
import moe.forpleuvoir.ibukigourd.render.RenderContext

interface State : UserInteractionHandler {

    val name: String

    fun onEnter()

    fun onExit()

    fun onRender(renderContext: RenderContext)

}



