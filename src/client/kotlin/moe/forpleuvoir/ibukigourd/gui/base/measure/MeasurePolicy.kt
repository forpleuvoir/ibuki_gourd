package moe.forpleuvoir.ibukigourd.gui.base.measure

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat

fun interface MeasurePolicy {

    fun measure(measurables: List<Measurable>, constraints: Constraints): SizeFloat

}