package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget

interface Layout : Measurable {

    val widget: IGWidget

    fun layoutableChildren(): List<Layoutable>

    var layoutCompleted: () -> Unit

    fun onLayoutCompleted() {
        layoutableChildren().filter { it is Layout }.forEach { (it as Layout).layoutCompleted() }
    }

    fun layout() {
        val layoutables = layoutableChildren()
        if (layoutables.isEmpty()) return
        layout(layoutables)
        layoutables.filter { it is Layout }.forEach { (it as Layout).layout() }
        layoutCompleted()
    }

    fun layout(layoutables: List<Layoutable>)

    //------------ Measure ------------\\

    fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable

    override fun measure(constraints: Constraints): Placeable =
        measureChildren(layoutableChildren(), this.constraints.constraintAs(constraints))

    override fun onMeasureCompleted() {
        layoutableChildren().forEach {
            it.measureCompleted()
        }
    }

}