package moe.forpleuvoir.ibukigourd.gui.base.modifier

import moe.forpleuvoir.ibukigourd.gui.base.element.Element

class PaddingModifier(val padding: moe.forpleuvoir.ibukigourd.gui.base.Padding) : ModifierImpl() {

    override var next: Modifier? = null

    override fun modify(element: Element) {
        element.padding = padding
    }

}

fun Modifier.padding(padding: moe.forpleuvoir.ibukigourd.gui.base.Padding) = this.then(PaddingModifier(padding))


fun Modifier.padding(
    left: Float = 0.0f,
    right: Float = 0.0f,
    top: Float = 0.0f,
    bottom: Float = 0.0f
) = this.then(PaddingModifier(moe.forpleuvoir.ibukigourd.gui.base.Padding(left, right, top, bottom)))


fun Modifier.padding(left: Number, right: Number, top: Number, bottom: Number) = padding(left.toFloat(), right.toFloat(), top.toFloat(), bottom.toFloat())

fun Modifier.padding(horizontal: Number = 0f, vertical: Number = 0f) = padding(horizontal, horizontal, vertical, vertical)

fun Modifier.padding(padding: Number) = padding(padding, padding, padding, padding)