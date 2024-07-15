package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

interface LinearLayout : Layout {

    companion object {

        fun create(orientation: Orientation, widget: WidgetContainerImpl): LinearLayout {
            return object : LinearLayout, Layout by widget {
                override val orientation: Orientation
                    get() = orientation

                override fun applyResult(size: Size<Float>): SizeFloat {
                    widget.transform.set(size.width, size.height)
                    return widget.transform
                }

                override fun measure(constraints: Constraints): Placeable {
                    return widget.measure(constraints)
                }

                override fun measure(measurables: List<Measurable>, constraints: Constraints): Placeable {
                    return widget.measure(measurables, constraints)
                }

                override fun layout() {
                    widget.layout()
                }

            }
        }

    }

    val orientation: Orientation

    override fun layout() {

    }

    override fun measure(measurables: List<Measurable>, constraints: Constraints): Placeable =
        orientation.peek(measureVertical(measurables, constraints), measureHorizontal(measurables, constraints))

    fun measureVertical(measurables: List<Measurable>, constraints: Constraints): Placeable {
        //测量所有


    }


    fun measureHorizontal(measurables: List<Measurable>, constraints: Constraints): Placeable {
        TODO()
    }


    override fun measure(constraints: Constraints): Placeable {
        return measure(measureChildren(), this.constraints.constraint(constraints))
    }

    fun applyResult(size: Size<Float>): SizeFloat

}


data class WrappedLinearLayoutData(
    val weight: Int
) {

    companion object {

        fun fromMeasurable(measurable: Measurable): WrappedLinearLayoutData? {
            return measurable.parentData?.let {
                if (it is WrappedLinearLayoutData) it else null
            }
        }

    }

}