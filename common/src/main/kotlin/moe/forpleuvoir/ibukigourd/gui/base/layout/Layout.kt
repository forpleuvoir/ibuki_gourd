package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.widget.GuiWidget

interface Layout : Measurable {

    val widget: GuiWidget

    fun layoutableChildren(): List<Layoutable>

    var layoutCompletion: () -> Unit

    fun onLayoutCompletion() {
        layoutableChildren().filter { it is Layout }.forEach { (it as Layout).layoutCompletion() }
    }

    fun layout() {
        val layoutables = layoutableChildren()
        if (layoutables.isEmpty()) return
        layout(layoutables)
        layoutables.filter { it is Layout }.forEach { (it as Layout).layout() }
        layoutCompletion()
    }

    fun layout(layoutables: List<Layoutable>)

    //------------ Measure ------------\\

    fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable

    override fun measure(constraints: Constraints): Placeable =
        measureChildren(layoutableChildren(), this.constraints.merge(constraints))

    override fun onMeasureCompletion() {
        layoutableChildren().forEach {
            it.measureCompletion()
        }
    }

}