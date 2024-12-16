package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.scaledSize
import org.joml.Vector2fc

interface AbsoluteLayout : Layout {
    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable {
        measurables.forEach { child ->
            child.measure(Constraints.of(maxSize = mc.window.scaledSize.toFloat()))
        }
        widget.transform.set(0f, 0f)
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
        override fun fromMeasurable(measurable: Measurable): WrappedAbsoluteLayoutData? =
            measurable.parentData as? WrappedAbsoluteLayoutData

        override fun default() = WrappedAbsoluteLayoutData()
    }

}