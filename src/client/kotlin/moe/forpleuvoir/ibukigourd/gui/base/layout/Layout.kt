package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.MeasurePolicy
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget

interface Layout : MeasurePolicy, Measurable {

    val widget: IGWidget

    fun measurableChildren(): List<Measurable>

    fun layout(placeables: List<Placeable>, parentDatas: List<WrappedLinearLayoutData?>)


}