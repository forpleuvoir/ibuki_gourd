package moe.forpleuvoir.ibukigourd.gui.base.modifier

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext

class RenderModifier(val render: (RenderContext) -> Unit) : ModifierImpl() {

    override var next: Modifier? = null

    override fun modify(element: Element) {
        element.render = render
    }
}

fun Modifier.render(render: (RenderContext) -> Unit): Modifier {
    return then(RenderModifier(render))
}