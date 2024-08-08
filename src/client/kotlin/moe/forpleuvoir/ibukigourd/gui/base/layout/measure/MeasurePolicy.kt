package moe.forpleuvoir.ibukigourd.gui.base.layout.measure

import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable

fun interface MeasurePolicy {

    fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable

    fun measureCompleted(measurables: List<Measurable>) {
        measurables.forEach {
            it.measureCompleted()
        }
    }

}