package moe.forpleuvoir.ibukigourd.gui.base.measure

import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat

interface Measurable : IntrinsicMeasurable {

    var constraints: Constraints

    fun measure(constraints: Constraints): SizeFloat

}