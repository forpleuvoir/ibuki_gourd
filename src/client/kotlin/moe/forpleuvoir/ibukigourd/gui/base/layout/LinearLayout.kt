package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.peek

interface LinearLayout : Layout {

    @Suppress("DuplicatedCode", "LocalVariableName")
    companion object {

        private fun LinearLayout.measureVertical(measurables: List<Measurable>, constraints: Constraints): Placeable {
            //垂直布局 宽度固定
            val (_minWidth, _maxWidth, _minHeight, _maxHeight) = this.constraints.constraint(constraints)
            //所有子元素的最大宽度限制固定
            val contentMaxWidth = (_maxWidth - widget.padding.width).coerceAtLeast(0f)
            //最宽的子元素宽度
            var maxChildWidth = 0f
            //内容的最大高度
            val contentMaxHeight = (_maxHeight - widget.padding.height).coerceAtLeast(0f)
            //所有元素的parentData
            val parentDatas = WrappedLinearLayoutData.wrappedDatas(measurables)
            //使用的高度
            var usedHeight = spacing * measurables.lastIndex
            //总权重
            var totalWidget = 0

            measurables.forEachIndexed { index, child ->
                val weight = parentDatas[index].weight
                if (weight > 0) {
                    totalWidget += weight
                } else {

                    val placeable = child.measure(
                        Constraints.of(
                            if (parentDatas[index].fill) contentMaxWidth - child.margin.width else 0f,
                            contentMaxWidth - child.margin.width,
                            0f,
                            (contentMaxHeight - usedHeight - child.margin.height).coerceAtLeast(0f)
                        )
                    )
                    if (placeable.size.width + child.margin.width > maxChildWidth) maxChildWidth = placeable.size.width + child.margin.width
                    usedHeight += placeable.size.height + child.margin.height
                }
            }

            val weightUnitHeight = if (totalWidget > 0) (contentMaxHeight - usedHeight) / totalWidget else 0f

            measurables.forEachIndexed { index, child ->
                val weight = parentDatas[index].weight
                if (weight > 0) {
                    val distributionHeight = ((weightUnitHeight * weight) - child.margin.height).coerceAtLeast(0f)
                    val placeable = child.measure(
                        Constraints.of(
                            if (parentDatas[index].fill) contentMaxWidth - child.margin.width else 0f,
                            contentMaxWidth - child.margin.width,
                            distributionHeight,
                            distributionHeight
                        )
                    )
                    if (placeable.size.width + child.margin.width > maxChildWidth) maxChildWidth = placeable.size.width + child.margin.width
                    usedHeight += placeable.size.height + child.margin.height
                }
            }
            usedHeight += widget.padding.height
            maxChildWidth += widget.padding.width
            return applyResult(maxChildWidth.coerceIn(_minWidth, _maxWidth), usedHeight.coerceIn(_minHeight, _maxHeight))
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
            //所有元素的parentData
            val parentDatas = WrappedLinearLayoutData.wrappedDatas(measurables)
            //使用的宽度
            var usedWidth = 0f
            //总权重
            var totalWidget = 0

            measurables.forEachIndexed { index, child ->
                val weight = parentDatas[index].weight
                if (weight > 0) {
                    totalWidget += weight

                } else {
                    val placeable = child.measure(
                        Constraints.of(
                            0f,
                            (contentMaxWidth - usedWidth - child.margin.width).coerceAtLeast(0f),
                            if (parentDatas[index].fill) contentMaxHeight - child.margin.height else 0f,
                            contentMaxHeight - child.margin.height
                        )
                    )
                    if (placeable.size.height + child.margin.height > maxChildHeight) maxChildHeight = placeable.size.height + child.margin.height
                    usedWidth += placeable.size.width + child.margin.width
                }
            }

            val weightUnitWidth = if (totalWidget > 0) (contentMaxWidth - usedWidth) / totalWidget else 0f

            measurables.forEachIndexed { index, child ->
                val weight = parentDatas[index].weight
                if (weight > 0) {
                    val distributionWidth = ((weightUnitWidth * weight) - child.margin.width).coerceAtLeast(0f)
                    val placeable = child.measure(
                        Constraints.of(
                            distributionWidth,
                            distributionWidth,
                            if (parentDatas[index].fill) contentMaxHeight - child.margin.height else 0f,
                            contentMaxHeight - child.margin.height
                        )
                    )
                    if (placeable.size.height + child.margin.height > maxChildHeight) maxChildHeight = placeable.size.height + child.margin.height
                    usedWidth += placeable.size.width + child.margin.width
                }
            }
            usedWidth += widget.padding.width + spacing * measurables.lastIndex
            maxChildHeight += widget.padding.height
            return applyResult(usedWidth.coerceIn(_minWidth, _maxWidth), maxChildHeight.coerceIn(_minHeight, _maxHeight))
        }

        private fun LinearLayout.layoutVertical(placeables: List<Placeable>, parentDatas: List<WrappedLinearLayoutData>) {
            alignment(orientation).align(
                widget.contentBox(false),
                placeables.mapIndexed { i, p -> Size(p.wrappedWidth, p.wrappedHeight + if (i != placeables.lastIndex) spacing else 0f) })
                .forEachIndexed { index, vector2fc ->
                    val placeable = placeables[index]
                    val gravity = parentDatas[index].gravity
                    val x = when (gravity) {
                        Gravity.Start  -> widget.padding.left + placeable.margin.left
                        Gravity.Center -> widget.transform.halfWidth - placeable.size.halfWidth
                        Gravity.End    -> widget.transform.width - widget.padding.right - placeable.size.width - placeable.margin.right
                    }
                    val y = vector2fc.y() + placeable.margin.top
                    placeable.placeAt(x, y, false)
                }
        }

        private fun LinearLayout.layoutHorizontal(placeables: List<Placeable>, parentDatas: List<WrappedLinearLayoutData>) {
            alignment(orientation).align(
                widget.contentBox(false),
                placeables.mapIndexed { i, p -> Size(p.wrappedWidth + if (i != placeables.lastIndex) spacing else 0f, p.wrappedHeight) })
                .forEachIndexed { index, vector2fc ->
                    val placeable = placeables[index]
                    val gravity = parentDatas[index].gravity
                    val y = when (gravity) {
                        Gravity.Start  -> widget.padding.top + placeable.margin.top
                        Gravity.Center -> widget.transform.halfHeight - placeable.size.halfHeight
                        Gravity.End    -> widget.transform.height - widget.padding.bottom - placeable.size.height - placeable.margin.bottom
                    }
                    val x = vector2fc.x() + placeable.margin.left
                    placeable.placeAt(x, y, false)
                }
        }

        @Suppress("nothing_to_inline")
        private inline fun LinearLayout.applyResult(width: Float, height: Float): Placeable {
            widget.transform.set(width, height)
            return widget
        }
    }

    var spacing: Float

    val orientation: Orientation

    val alignment: (Orientation) -> Alignment

    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable =
        orientation.peek(
            { measureVertical(measurables, constraints) },
            { measureHorizontal(measurables, constraints) }
        )

    override fun layout(layoutables: List<Layoutable>) {
        val datas = WrappedLinearLayoutData.wrappedDatas(layoutables)
        orientation.peek(
            { layoutVertical(layoutables, datas) },
            { layoutHorizontal(layoutables, datas) }
        )
    }

}

interface RowLayout : LinearLayout {
    override val orientation: Orientation get() = Orientation.Vertical

}

interface ColumnLayout : LinearLayout {
    override val orientation: Orientation get() = Orientation.Horizontal

}

data class WrappedLinearLayoutData(
    val weight: Int = 0,
    val fill: Boolean = false,
    var gravity: Gravity = Gravity.Center
) {

    companion object : WrappedLayoutDataUtil<WrappedLinearLayoutData> {
        override fun default() = WrappedLinearLayoutData()

    }

}