package moe.forpleuvoir.ibukigourd.gui.base.measure

import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat

interface Measurable : IntrinsicMeasurable {

    fun measure(constraints: Constraints): SizeFloat

}