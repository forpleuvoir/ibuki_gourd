package moe.forpleuvoir.ibukigourd.gui.base.measure

import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat

interface Measurable {

    var constraints: Constraints

    val parentData: Any?

    fun measure(constraints: Constraints): SizeFloat

}