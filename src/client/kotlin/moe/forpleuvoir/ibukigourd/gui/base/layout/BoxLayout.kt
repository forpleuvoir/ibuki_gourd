package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.math.plus

interface BoxLayout : Layout {

    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable {
        val (minWidth, maxWidth, minHeight, maxHeight) = this.constraints.constraintAs(constraints)
        var maxChildWidth = 0f
        var maxChildHeight = 0f

        val parentData = WrappedBoxLayoutData.wrappedData(measurables)

        val childConstraints = Constraints.of(0f, maxWidth - widget.padding.width, 0f, maxHeight - widget.padding.height)
        measurables.forEachIndexed { index, child ->
            val data = parentData[index]
            var _childConstraints = childConstraints
            if (data.fillWidth) {
                val w = (maxWidth - widget.padding.width).coerceAtLeast(0f)
                _childConstraints = _childConstraints.copy(minWidth = w, maxWidth = w)
            }
            if (data.fillHeight) {
                val h = (maxHeight - widget.padding.height).coerceAtLeast(0f)
                _childConstraints = _childConstraints.copy(minHeight = h, maxHeight = h)
            }
            child.measure(_childConstraints).also {
                if (it.wrappedWidth > maxChildWidth) maxChildWidth = it.wrappedWidth
                if (it.wrappedHeight > maxChildHeight) maxChildHeight = it.wrappedHeight
            }
        }

        maxChildWidth += widget.padding.width
        maxChildHeight += widget.padding.height
        widget.transform.set(
            maxChildWidth.coerceIn(minWidth, maxWidth),
            maxChildHeight.coerceIn(minHeight, maxHeight)
        )
        return widget
    }

    override fun layout(layoutables: List<Layoutable>) {
        val data = WrappedBoxLayoutData.wrappedData(layoutables)
        layoutables.forEachIndexed { index, placeable ->
            val vec2f = data[index].alignment.align(widget.contentBox(false), placeable.wrappedSize)
            placeable.placeAt(vec2f + Vector2f(placeable.margin.left, placeable.margin.top) + Vector2f(widget.padding.left, widget.padding.top), false)
        }
    }

}

data class WrappedBoxLayoutData(
    val alignment: Alignment = Alignment.Center,
    val fillWidth: Boolean = false,
    val fillHeight: Boolean = false
) {

    companion object : WrappedLayoutDataUtil<WrappedBoxLayoutData> {
        override fun fromMeasurable(measurable: Measurable): WrappedBoxLayoutData? =
            measurable.parentData as? WrappedBoxLayoutData

        override fun default() = WrappedBoxLayoutData()

    }

}