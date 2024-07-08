package moe.forpleuvoir.ibukigourd.gui.base.modifier

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext

class RenderModifier(val renderAction: Element.(RenderContext) -> Unit) : ModifierImpl() {

    override var next: Modifier? = null

    override fun modify(element: Element) {
        element.render = {
            element.apply {
                this.renderAction(it)
            }
        }
    }
}

fun Modifier.render(render: Element.(RenderContext) -> Unit): Modifier {
    return then(RenderModifier(render))
}