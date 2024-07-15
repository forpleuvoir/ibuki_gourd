package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat

interface LinearLayout : Layout {

    override fun measure(measurables: List<Measurable>, constraints: Constraints): SizeFloat {
        TODO("Not yet implemented")
    }

    override fun measure(constraints: Constraints): SizeFloat {
        return measure(measureChildren(), this.constraints.constraint(constraints))
    }

}