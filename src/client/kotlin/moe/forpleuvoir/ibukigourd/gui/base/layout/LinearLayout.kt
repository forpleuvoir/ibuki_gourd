package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable

interface LinearLayout : Layout {

    val arrangement: Arrangement.Linear

    val alignment: Alignment.Linear

}

interface WrappedLinearLayoutData {
    val weight: Int
    val fill: Boolean
    val alignment: Alignment.Linear?
}


interface RowLayout : LinearLayout {

    data class WrappedRowLayoutData(
        override val weight: Int = 0,
        override val fill: Boolean = false,
        override val alignment: Alignment.Horizontal? = null
    ) : WrappedLinearLayoutData {
        companion object : WrappedLayoutDataUtil<WrappedRowLayoutData> {
            override fun default() = WrappedRowLayoutData()
        }

        fun getAlignment(layout: RowLayout) = this.alignment ?: layout.alignment
    }

    override val arrangement: Arrangement.Vertical

    override val alignment: Alignment.Horizontal

    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable {
        //垂直布局 宽度固定
        val (minWidth, maxWidth, minHeight, maxHeight) = this.constraints.constraintAs(constraints)
        //所有子元素的最大宽度限制固定
        val contentMaxWidth = (maxWidth - widget.padding.width).coerceAtLeast(0f)
        //最宽的子元素宽度
        var maxChildWidth = 0f
        //内容的最大高度
        val contentMaxHeight = (maxHeight - widget.padding.height).coerceAtLeast(0f)
        //所有元素的parentData
        val parentDatas = WrappedRowLayoutData.wrappedDatas(measurables)
        //使用的高度
        var usedHeight = arrangement.spacing * measurables.lastIndex
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
        widget.transform.set(maxChildWidth.coerceIn(minWidth, maxWidth), usedHeight.coerceIn(minHeight, maxHeight))
        return widget
    }

    override fun layout(layoutables: List<Layoutable>) {
        val contentBox = widget.contentBox(false)
        val horizontalSpace = contentBox.width
        val verticalSpace = contentBox.height

        val alignments = WrappedRowLayoutData.wrappedDatas(layoutables).map { it.getAlignment(this) }

        val verticalOffsets = arrangement.arrange(verticalSpace, layoutables.map { it.wrappedHeight }).map { contentBox.top + it }
        val horizontalOffsets = layoutables.mapIndexed { i, l -> contentBox.left + alignments[i].align(horizontalSpace, l.wrappedHeight) }

        horizontalOffsets.zip(verticalOffsets).forEachIndexed { i, (x, y) ->
            val p = layoutables[i]
            val margin = p.margin
            p.placeAt(x + margin.left, y + margin.top, false)
        }
    }

}

interface ColumnLayout : LinearLayout {

    data class WrappedColumnLayoutData(
        override val weight: Int = 0,
        override val fill: Boolean = false,
        override val alignment: Alignment.Vertical? = null
    ) : WrappedLinearLayoutData {
        companion object : WrappedLayoutDataUtil<WrappedColumnLayoutData> {
            override fun default() = WrappedColumnLayoutData()
        }

        fun getAlignment(layout: ColumnLayout) = this.alignment ?: layout.alignment
    }

    override val arrangement: Arrangement.Horizontal

    override val alignment: Alignment.Vertical

    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable {
        //水平布局 高度固定
        val (minWidth, maxWidth, minHeight, maxHeight) = this.constraints.constraintAs(constraints)
        //所有子元素的最大高度限制固定
        val contentMaxHeight = (maxHeight - widget.padding.height).coerceAtLeast(0f)
        //最高的子元素高度
        var maxChildHeight = 0f
        //内容的最大高度
        val contentMaxWidth = (maxWidth - widget.padding.width).coerceAtLeast(0f)
        //所有元素的parentData
        val parentDatas = WrappedColumnLayoutData.wrappedDatas(measurables)
        //使用的宽度
        var usedWidth = arrangement.spacing * measurables.lastIndex
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
        usedWidth += widget.padding.width
        maxChildHeight += widget.padding.height
        widget.transform.set(usedWidth.coerceIn(minWidth, maxWidth), maxChildHeight.coerceIn(minHeight, maxHeight))
        return widget
    }

    override fun layout(layoutables: List<Layoutable>) {
        val contentBox = widget.contentBox(false)
        val horizontalSpace = contentBox.width
        val verticalSpace = contentBox.height

        val alignments = WrappedColumnLayoutData.wrappedDatas(layoutables, WrappedColumnLayoutData()).map { it.getAlignment(this) }

        val verticalOffsets = layoutables.mapIndexed { i, l -> contentBox.left + alignments[i].align(horizontalSpace, l.wrappedHeight) }
        val horizontalOffsets = arrangement.arrange(verticalSpace, layoutables.map { it.wrappedHeight }).map { contentBox.top + it }

        horizontalOffsets.zip(verticalOffsets).forEachIndexed { i, (x, y) ->
            val p = layoutables[i]
            val margin = p.margin
            p.placeAt(x + margin.left, y + margin.top, false)
        }
    }

}

