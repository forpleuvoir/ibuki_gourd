package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.BoxAlignment
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.render.math.plus

interface BoxLayout : Layout {

    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable {
        val (_minWidth, _maxWidth, _minHeight, _maxHeight) = this.constraints.constraint(constraints)
        var maxChildWidth = 0f
        var maxChildHeight = 0f
        val parentDatas = measurables.map { WrappedBoxLayoutData.getOrDefault(it) }
        var maxLeft = 0f
        var maxCenterWidth = 0f
        var maxRight = 0f
        var maxTop = 0f
        var maxCenterHeight = 0f
        var maxBottom = 0f

        val placeables = measurables.mapIndexed { index, child ->
            var childConstraints = Constraints.of(0f, _maxWidth - widget.padding.width, 0f, _maxHeight - widget.padding.height)
            val data = parentDatas[index]
            if (data.fillWidth) {
                val w = (_maxWidth - widget.padding.width).coerceAtLeast(0f)
                childConstraints = childConstraints.copy(minWidth = w, maxWidth = w)
            }
            if (data.fillHeight) {
                val h = (_maxHeight - widget.padding.height).coerceAtLeast(0f)
                childConstraints = childConstraints.copy(minHeight = h, maxHeight = h)
            }
            val placeable = child.measure(childConstraints).also {
                if (it.wrappedWidth > maxChildWidth) maxChildWidth = it.wrappedWidth
                if (it.wrappedHeight > maxChildHeight) maxChildHeight = it.wrappedHeight
            }
            //------------ 测量最大尺寸 ------------\\
            data.alignment.run {
                if (this is BoxAlignment.Center && this !is BoxAlignment.Horizontal && this !is BoxAlignment.Vertical) {
                    if (maxCenterWidth < placeable.wrappedWidth)
                        maxCenterWidth = placeable.wrappedWidth
                    if (maxCenterHeight < placeable.wrappedHeight)
                        maxCenterHeight = placeable.wrappedHeight
                    return@run
                }
                when (this) {
                    is BoxAlignment.Left   -> if (maxLeft < placeable.wrappedWidth) {
                        maxLeft = placeable.wrappedWidth
                    }

                    is BoxAlignment.Right  -> if (maxRight < placeable.wrappedWidth) {
                        maxRight = placeable.wrappedWidth
                    }

                    is BoxAlignment.Top    -> if (maxTop < placeable.wrappedHeight) {
                        maxTop = placeable.wrappedHeight
                    }

                    is BoxAlignment.Bottom -> if (maxBottom < placeable.wrappedHeight) {
                        maxBottom = placeable.wrappedHeight
                    }

                    else                   -> Unit
                }
            }

            placeable
        }
        //计算内容宽度
        val width = maxLeft + maxCenterWidth + maxRight + widget.padding.width

        //计算内容高度
        val height = maxTop + maxCenterHeight + maxBottom + widget.padding.height

        maxChildWidth += widget.padding.width
        maxChildHeight += widget.padding.height
        widget.transform.set(
            maxChildWidth.coerceAtLeast(width).coerceIn(_minWidth, _maxWidth),
            maxChildHeight.coerceAtLeast(height).coerceIn(_minHeight, _maxHeight)
        )
        return widget
    }

    override fun layout(layoutables: List<Layoutable>) {
        val datas = layoutables.map { WrappedBoxLayoutData.getOrDefault(it) }
        layoutables.forEachIndexed { index, placeable ->
            val vec2f = datas[index].alignment.align(widget.contentBox(false), placeable.wrappedSize)
            placeable.placeAt(vec2f + Vector2f(placeable.margin.left, placeable.margin.top), false)
        }
    }

}

data class WrappedBoxLayoutData(
    val alignment: BoxAlignment = BoxAlignment.CenterCenter(),
    val fillWidth: Boolean = false,
    val fillHeight: Boolean = false
) {

    companion object {

        private val default = WrappedBoxLayoutData()

        fun fromMeasurable(measurable: Measurable) =
            measurable.parentData as? WrappedBoxLayoutData


        fun getOrDefault(measurable: Measurable, default: WrappedBoxLayoutData = this.default) =
            fromMeasurable(measurable) ?: default


    }
}