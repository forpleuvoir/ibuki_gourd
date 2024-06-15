package moe.forpleuvoir.ibukigourd.gui.base.modifier

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementDimension

class DimensionModifier(val dimension: ElementDimension, private val isWidth: Boolean) : ModifierImpl() {
    override fun modify(element: Element) {
        if (isWidth) {
            element.width = dimension
        } else {
            element.height = dimension
        }
    }

}

fun Modifier.width(dimension: ElementDimension): Modifier = this.then(DimensionModifier(dimension, true))


fun Modifier.height(dimension: ElementDimension): Modifier = this.then(DimensionModifier(dimension, false))
