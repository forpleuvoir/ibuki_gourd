package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import org.joml.Vector2fc

interface AbsoluteLayout : Layout {
    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable {
        val (_, _maxWidth, _, _maxHeight) = this.constraints.constraintAs(constraints)
        measurables.forEachIndexed { index, child ->
            val childConstraints = Constraints.of(
                0f, _maxWidth - child.margin.width,
                0f, _maxHeight - child.margin.height
            )
            child.measure(childConstraints)
        }
        return widget
    }

    override fun layout(layoutables: List<Layoutable>) {
        val datas = WrappedAbsoluteLayoutData.wrappedDatas(layoutables)
        layoutables.forEachIndexed { index, layoutable ->
            layoutable.placeAt(datas[index].asVec2, true)
        }
    }

}

data class WrappedAbsoluteLayoutData(
    val x: Float = 0f,
    val y: Float = 0f,
) {

    val asVec2: Vector2fc = Vector2f(x, y)

    companion object : WrappedLayoutDataUtil<WrappedAbsoluteLayoutData> {
        override fun default() = WrappedAbsoluteLayoutData()

    }

}