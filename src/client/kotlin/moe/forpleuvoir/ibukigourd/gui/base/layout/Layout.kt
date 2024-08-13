package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.MeasurePolicy
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget

interface Layout : MeasurePolicy, Measurable {

    val widget: IGWidget

    fun layoutableChildren(): List<Layoutable>

    fun layout() {
        val layoutables = layoutableChildren()
        if (layoutables.isEmpty()) return
        layout(layoutables)
        layoutableChildren().filter { it is Layout }.forEach { (it as Layout).layout() }
    }

    fun layout(layoutables: List<Layoutable>)

    override fun measure(constraints: Constraints): Placeable =
        measureChildren(layoutableChildren(), this.constraints.constraintAs(constraints))

    override fun measureCompleted() {
        measureCompleted(layoutableChildren())
    }

}