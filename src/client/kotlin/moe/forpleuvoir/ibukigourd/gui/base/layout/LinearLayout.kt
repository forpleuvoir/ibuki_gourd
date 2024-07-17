package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.scope.LinearLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl

interface LinearLayout : Layout {

    @Suppress("DuplicatedCode", "LocalVariableName")
    companion object {

        fun create(orientation: Orientation, alignment: (Orientation) -> Alignment, widget: WidgetContainerImpl): LinearLayout {
            return object : LinearLayout, Layout by widget {
                override val orientation: Orientation
                    get() = orientation

                override val alignment: (Orientation) -> Alignment
                    get() = alignment

                override fun measure(constraints: Constraints): Placeable {
                    return widget.measure(constraints)
                }

                override fun measure(measurables: List<Measurable>, constraints: Constraints): Placeable {
                    return widget.measure(measurables, constraints)
                }

                override fun layout(placeables: List<Placeable>, parentDatas: List<WrappedLinearLayoutData?>) {
                    widget.layout(placeables, parentDatas)
                }

            }
        }

        private fun LinearLayout.measureVertical(measurables: List<Measurable>, constraints: Constraints): Placeable {
            //垂直布局 宽度固定
            val (_minWidth, _maxWidth, _minHeight, _maxHeight) = this.constraints.constraint(constraints)
            //所有子元素的最大宽度限制固定
            val contentMaxWidth = (_maxWidth - widget.padding.width).coerceAtLeast(0f)
            //最宽的子元素宽度
            var maxChildWidth = 0f
            //内容的最大高度
            val contentMaxHeight = (_maxHeight - widget.padding.height).coerceAtLeast(0f)
            //可放置元素
            val placeables = arrayOfNulls<Placeable>(measurables.size)
            //所有元素的parentData
            val parentDatas = measurables.map { WrappedLinearLayoutData.fromMeasurable(it) }
            //使用的高度
            var usedHeight = 0f
            //总权重
            var totalWidget = 0
            //拥有权重的子元素
            var weightChildrenCount = 0

            measurables.forEachIndexed { index, child ->
                val weight = parentDatas[index]?.weight
                if (weight != null) {
                    totalWidget += weight
                    weightChildrenCount++
                } else {
                    val placeable = child.measure(
                        Constraints(
                            0f,
                            contentMaxWidth - child.margin.width,
                            0f,
                            (contentMaxHeight - usedHeight - child.margin.height).coerceAtLeast(0f)
                        )
                    )
                    if (placeable.size.width + child.margin.width > maxChildWidth) maxChildWidth = placeable.size.width + child.margin.width
                    usedHeight += placeable.size.height + child.margin.height
                    placeables[index] = placeable
                }
            }

            val weightUnitHeight = if (totalWidget > 0) (contentMaxHeight - usedHeight) / totalWidget else 0f

            measurables.forEachIndexed { index, child ->
                val widget = parentDatas[index]?.weight
                if (widget != null) {
                    val distributionHeight = ((weightUnitHeight * widget) - child.margin.height).coerceAtLeast(0f)
                    val placeable = child.measure(
                        Constraints(
                            0f,
                            contentMaxWidth - child.margin.width,
                            distributionHeight,
                            distributionHeight
                        )
                    )
                    if (placeable.size.width + child.margin.width > maxChildWidth) maxChildWidth = placeable.size.width + child.margin.width
                    usedHeight += placeable.size.height + child.margin.height
                    placeables[index] = placeable
                }
            }
            usedHeight += widget.padding.height
            maxChildWidth += widget.padding.width
            applyResult(Size(maxChildWidth.coerceIn(_minWidth, _maxWidth), usedHeight.coerceIn(_minHeight, _maxHeight))).let { placeable ->
                layout(placeables.map { it!! }, parentDatas)
                return placeable
            }
        }

        private fun LinearLayout.measureHorizontal(measurables: List<Measurable>, constraints: Constraints): Placeable {
            //水平布局 高度固定
            val (_minWidth, _maxWidth, _minHeight, _maxHeight) = this.constraints.constraint(constraints)
            //所有子元素的最大高度限制固定
            val contentMaxHeight = (_maxHeight - widget.padding.height).coerceAtLeast(0f)
            //最高的子元素高度
            var maxChildHeight = 0f
            //内容的最大高度
            val contentMaxWidth = (_maxWidth - widget.padding.width).coerceAtLeast(0f)
            //可放置元素
            val placeables = arrayOfNulls<Placeable>(measurables.size)
            //所有元素的parentData
            val parentDatas = measurables.map { WrappedLinearLayoutData.fromMeasurable(it) }
            //使用的宽度
            var usedWidth = 0f
            //总权重
            var totalWidget = 0
            //拥有权重的子元素
            var weightChildrenCount = 0

            measurables.forEachIndexed { index, child ->
                val weight = parentDatas[index]?.weight
                if (weight != null) {
                    totalWidget += weight
                    weightChildrenCount++
                } else {
                    val placeable = child.measure(
                        Constraints(
                            0f,
                            (contentMaxWidth - usedWidth - child.margin.width).coerceAtLeast(0f),
                            0f,
                            contentMaxHeight - child.margin.height
                        )
                    )
                    if (placeable.size.height + child.margin.height > maxChildHeight) maxChildHeight = placeable.size.height + child.margin.height
                    usedWidth += placeable.size.width + child.margin.width
                    placeables[index] = placeable
                }
            }

            val weightUnitWidth = if (totalWidget > 0) (contentMaxWidth - usedWidth) / totalWidget else 0f

            measurables.forEachIndexed { index, child ->
                val widget = parentDatas[index]?.weight
                if (widget != null) {
                    val distributionWidth = ((weightUnitWidth * widget) - child.margin.width).coerceAtLeast(0f)
                    val placeable = child.measure(
                        Constraints(
                            distributionWidth,
                            distributionWidth,
                            0f,
                            contentMaxHeight - child.margin.height,
                        )
                    )
                    if (placeable.size.height + child.margin.height > maxChildHeight) maxChildHeight = placeable.size.height + child.margin.height
                    usedWidth += placeable.size.width + child.margin.width
                    placeables[index] = placeable
                }
            }
            usedWidth += widget.padding.width
            maxChildHeight += widget.padding.height
            applyResult(Size(usedWidth.coerceIn(_minWidth, _maxWidth), maxChildHeight.coerceIn(_minHeight, _maxHeight))).let { placeable ->
                layout(placeables.map { it!! }, parentDatas)
                return placeable
            }
        }

        private fun LinearLayout.layoutVertical(placeables: List<Placeable>, parentDatas: List<WrappedLinearLayoutData?>) {
            val contentBox = widget.contentBox(false)
            alignment(orientation).align(contentBox, placeables.map { Size(it.size.width + it.margin.width, it.size.height + it.margin.height) })
                .forEachIndexed { index, vector2fc ->
                    val placeable = placeables[index]
                    val gravity = parentDatas[index]?.gravity ?: WrappedLinearLayoutData.Gravity.Center
                    val x = when (gravity) {
                        WrappedLinearLayoutData.Gravity.Start  -> widget.padding.left + placeable.margin.left
                        WrappedLinearLayoutData.Gravity.Center -> widget.transform.halfWidth - (placeable.margin.left + placeable.size.halfWidth)
                        WrappedLinearLayoutData.Gravity.End    -> widget.transform.width - widget.padding.right - placeable.size.width - placeable.margin.right
                    }
                    val y = vector2fc.y() + placeable.margin.top
                    placeable.placeAt(x, y, false)
                }
        }

        private fun LinearLayout.layoutHorizontal(placeables: List<Placeable>, parentDatas: List<WrappedLinearLayoutData?>) {
            val contentBox = widget.contentBox(false)
            alignment(orientation).align(contentBox, placeables.map { Size(it.size.width + it.margin.width, it.size.height + it.margin.height) })
                .forEachIndexed { index, vector2fc ->
                    val placeable = placeables[index]
                    val gravity = parentDatas[index]?.gravity ?: WrappedLinearLayoutData.Gravity.Center
                    val y = when (gravity) {
                        WrappedLinearLayoutData.Gravity.Start -> widget.padding.top + placeable.margin.top
                        WrappedLinearLayoutData.Gravity.Center -> widget.transform.halfHeight - (placeable.margin.top + placeable.size.halfHeight)
                        WrappedLinearLayoutData.Gravity.End   -> widget.transform.height - widget.padding.bottom - placeable.size.height - placeable.margin.bottom
                    }
                    val x = vector2fc.x() + placeable.margin.left
                    placeable.placeAt(x, y, false)
                }
        }
    }


    val orientation: Orientation

    val alignment: (Orientation) -> Alignment

    override fun measure(measurables: List<Measurable>, constraints: Constraints): Placeable =
        orientation.peek(
            { measureVertical(measurables, constraints) },
            { measureHorizontal(measurables, constraints) }
        )

    override fun measure(constraints: Constraints): Placeable =
        measure(measurableChildren(), this.constraints.constraint(constraints))


    override fun layout(placeables: List<Placeable>, parentDatas: List<WrappedLinearLayoutData?>) {
        if (placeables.isEmpty()) return
        orientation.peek(
            { layoutVertical(placeables, parentDatas) },
            { layoutHorizontal(placeables, parentDatas) }
        )
    }

    fun applyResult(size: Size<Float>): Placeable {
        widget.transform.set(size.width, size.height)
        return widget
    }

}

interface RowLayout : LinearLayout {
    override val orientation: Orientation get() = Orientation.Vertical

}

interface ColumnLayout : LinearLayout {
    override val orientation: Orientation get() = Orientation.Horizontal

}

data class WrappedLinearLayoutData(
    val weight: Int? = null,
    var gravity: Gravity = Gravity.Center
) {

    enum class Gravity {
        Start, Center, End;
    }

    companion object {

        fun fromMeasurable(measurable: Measurable): WrappedLinearLayoutData? {
            return measurable.parentData as? WrappedLinearLayoutData
        }

    }

}