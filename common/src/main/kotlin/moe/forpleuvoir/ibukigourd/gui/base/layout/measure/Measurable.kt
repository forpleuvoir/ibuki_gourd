package moe.forpleuvoir.ibukigourd.gui.base.layout.measure

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable

interface Measurable {

    var constraints: Constraints

    val margin: Margin

    var parentData: Any?

    fun measure(constraints: Constraints): Placeable

    var measureCompletion: () -> Unit

    fun onMeasureCompletion()

    fun remeasure()

}