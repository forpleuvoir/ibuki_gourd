package moe.forpleuvoir.ibukigourd.gui.base.modifier

import moe.forpleuvoir.ibukigourd.gui.base.element.Element

class MarginModifier(val margin: moe.forpleuvoir.ibukigourd.gui.base.Margin) : ModifierImpl() {

    override var next: Modifier? = null

    override fun modify(element: Element) {
        element.margin = margin
    }


}

fun Modifier.margin(margin: moe.forpleuvoir.ibukigourd.gui.base.Margin): Modifier = this.then(PaddingModifier(margin))

fun Modifier.margin(
    left: Float = 0.0f,
    right: Float = 0.0f,
    top: Float = 0.0f,
    bottom: Float = 0.0f
): Modifier = this.then(PaddingModifier(moe.forpleuvoir.ibukigourd.gui.base.Margin(left, right, top, bottom)))


fun Modifier.margin(left: Number, right: Number, top: Number, bottom: Number) = margin(left.toFloat(), right.toFloat(), top.toFloat(), bottom.toFloat())

fun Modifier.margin(horizontal: Number = 0f, vertical: Number = 0f) = margin(horizontal, horizontal, vertical, vertical)

fun Modifier.margin(margin: Number) = margin(margin, margin, margin, margin)