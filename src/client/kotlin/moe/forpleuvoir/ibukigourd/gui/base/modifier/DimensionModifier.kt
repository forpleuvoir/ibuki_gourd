package moe.forpleuvoir.ibukigourd.gui.base.modifier

import moe.forpleuvoir.ibukigourd.gui.base.element.Element
import moe.forpleuvoir.ibukigourd.gui.base.element.ElementDimension

class DimensionModifier(private val width: ElementDimension?, private val height: ElementDimension?) : ModifierImpl() {
    override fun modify(element: Element) {
        width?.let { element.width = it }
        height?.let { element.height = it }
    }

}

fun Modifier.width(dimension: ElementDimension): Modifier = this.then(DimensionModifier(width = dimension, height = null))

fun Modifier.height(dimension: ElementDimension): Modifier = this.then(DimensionModifier(width = null, height = dimension))

fun Modifier.dimension(width: ElementDimension? = null, height: ElementDimension? = null) =
    this.then(DimensionModifier(width, height))
