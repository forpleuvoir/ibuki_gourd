package moe.forpleuvoir.ibukigourd.gui.base.layout.measure

import moe.forpleuvoir.ibukigourd.gui.base.layout.Placeable

interface Measurable {

    var constraints: Constraints

    var parentData: Any?

    fun measure(constraints: Constraints): Placeable

}