package moe.forpleuvoir.ibukigourd.gui.base.layout.measure

import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable

fun interface MeasurePolicy {

    fun measure(measurables: List<Measurable>, constraints: Constraints): Placeable

}