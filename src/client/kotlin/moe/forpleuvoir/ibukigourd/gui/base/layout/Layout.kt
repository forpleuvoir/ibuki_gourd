package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.measure.MeasurePolicy

interface Layout : MeasurePolicy, Measurable {

    fun measureChildren(): List<Measurable>


}