package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.MeasurePolicy

interface Layout : MeasurePolicy, Measurable {

    fun measureChildren(): List<Measurable>

    fun layout()


}