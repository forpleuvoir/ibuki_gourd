package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.layout.util.FillMode

interface LinearLayout<A : Arrangement.Linear, B : Alignment.Linear> : Layout {

    val arrangement: A

    val alignment: B

}

interface WrappedLinearLayoutData {
    /**
     * 表示子组件在父组件中所占的权重。
     * 该值用于确定多个子组件在父组件中的相对空间分配。
     * 值越大，子组件在父组件中所占的空间比例越大。
     */
    val weight: Int

    /**
     * 表示在父组件内部子组件的填充模式。
     * 可以使用不同的填充模式来确定子组件如何在容器中占据空间。
     *
     * 例如：
     * - `FillMode.None`：不进行填充。
     * - `FillMode.MatchParent`：填充整个父组件。
     * - `FillMode.MatchSibling`：填充至与其他兄弟组件相等的尺寸。
     */
    val fillMode: FillMode

    /**
     * 控制子组件如何在父组件中对齐的参数。
     * 其类型为 `Alignment.Linear?`，表示它可以为空，
     * 代表子组件的对齐方式可以是线性的或未定义的。
     *
     * 例如，可以用来确定子组件是水平居中、左对齐还是右对齐等。
     */
    val alignment: Alignment.Linear?
}

interface RowLayout : LinearLayout<Arrangement.Vertical, Alignment.Horizontal> {

    data class WrappedRowLayoutData(
        override val weight: Int = 0,
        override val fillMode: FillMode = FillMode.None,
        override val alignment: Alignment.Horizontal? = null
    ) : WrappedLinearLayoutData {
        companion object : WrappedLayoutDataUtil<WrappedRowLayoutData> {
            override fun fromMeasurable(measurable: Measurable): WrappedRowLayoutData? =
                measurable.parentData as? WrappedRowLayoutData

            override fun default() = WrappedRowLayoutData()
        }

        fun getAlignment(layout: RowLayout) = this.alignment ?: layout.alignment
    }

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
        val totalWidget = parentDatas.sumOf { it.weight }

        //优先计算固定最小尺寸的组件的占用
        var minimumOccupiedHeight = 0f
        measurables.forEachIndexed { index, child ->
            if (parentDatas[index].run { weight <= 0 && fillMode == FillMode.MatchSibling }) {
                minimumOccupiedHeight += child.constraints.minHeight + child.margin.height
            }
        }

        measurables.forEachIndexed { index, child ->
            if (parentDatas[index].run { weight <= 0 && fillMode != FillMode.MatchSibling }) {
                val placeable = child.measure(
                    Constraints.of(
                        if (parentDatas[index].fillMode == FillMode.MatchParent) contentMaxWidth - child.margin.width else 0f,
                        contentMaxWidth - child.margin.width,
                        0f,
                        (contentMaxHeight - usedHeight - minimumOccupiedHeight - child.margin.height).coerceAtLeast(0f)
                    )
                )
                if (placeable.size.width + child.margin.width > maxChildWidth) maxChildWidth = placeable.size.width + child.margin.width
                usedHeight += placeable.size.height + child.margin.height
            }
        }

        measurables.forEachIndexed { index, child ->
            if (parentDatas[index].run { weight <= 0 && fillMode == FillMode.MatchSibling }) {
                val max = if (maxChildWidth > 0) maxChildWidth - child.margin.width else contentMaxWidth - child.margin.width
                val min = if (maxChildWidth > 0) maxChildWidth - child.margin.width else 0f
                val placeable = child.measure(
                    Constraints.of(
                        min, max,
                        0f,
                        (contentMaxHeight - usedHeight - child.margin.height).coerceAtLeast(0f)
                    )
                )
                if (placeable.size.width + child.margin.width > maxChildWidth) maxChildWidth = placeable.size.width + child.margin.width
                usedHeight += placeable.size.height + child.margin.height
            }
        }

        //单位权重所占的高度
        val weightUnitHeight = if (totalWidget > 0) (contentMaxHeight - usedHeight) / totalWidget else 0f

        measurables.forEachIndexed { index, child ->
            if (parentDatas[index].run { weight > 0 && fillMode != FillMode.MatchSibling }) {
                val weight = parentDatas[index].weight
                val distributionHeight = ((weightUnitHeight * weight) - child.margin.height).coerceAtLeast(0f)
                val placeable = child.measure(
                    Constraints.of(
                        if (parentDatas[index].fillMode == FillMode.MatchParent) contentMaxWidth - child.margin.width else 0f,
                        contentMaxWidth - child.margin.width,
                        distributionHeight, distributionHeight
                    )
                )
                if (placeable.size.width + child.margin.width > maxChildWidth) maxChildWidth = placeable.size.width + child.margin.width
                usedHeight += placeable.size.height + child.margin.height
            }
        }

        measurables.forEachIndexed { index, child ->
            if (parentDatas[index].run { weight > 0 && fillMode == FillMode.MatchSibling }) {
                val weight = parentDatas[index].weight
                val distributionHeight = ((weightUnitHeight * weight) - child.margin.height).coerceAtLeast(0f)
                val min = if (maxChildWidth > 0) maxChildWidth - child.margin.width else 0f
                val max = if (maxChildWidth > 0) maxChildWidth - child.margin.width else contentMaxWidth - child.margin.width
                val placeable = child.measure(
                    Constraints.of(min, max, distributionHeight, distributionHeight)
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
        val horizontalOffsets = layoutables.mapIndexed { i, l ->
            contentBox.left + alignments[i].align(horizontalSpace, l.wrappedWidth)
        }

        horizontalOffsets.zip(verticalOffsets).forEachIndexed { i, (x, y) ->
            val p = layoutables[i]
            val margin = p.margin
            p.placeAt(x + margin.left, y + margin.top, false)
        }
    }

}

interface ColumnLayout : LinearLayout<Arrangement.Horizontal, Alignment.Vertical> {

    data class WrappedColumnLayoutData(
        override val weight: Int = 0,
        override val fillMode: FillMode = FillMode.None,
        override val alignment: Alignment.Vertical? = null
    ) : WrappedLinearLayoutData {
        companion object : WrappedLayoutDataUtil<WrappedColumnLayoutData> {
            override fun fromMeasurable(measurable: Measurable): WrappedColumnLayoutData? =
                measurable.parentData as? WrappedColumnLayoutData

            override fun default() = WrappedColumnLayoutData()
        }

        fun getAlignment(layout: ColumnLayout) = this.alignment ?: layout.alignment
    }

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
        val totalWidget = parentDatas.sumOf { it.weight }

        //优先计算固定最小尺寸的组件的占用
        var minimumOccupiedWidth = 0f
        measurables.forEachIndexed { index, child ->
            if (parentDatas[index].run { weight <= 0 && fillMode == FillMode.MatchSibling }) {
                minimumOccupiedWidth += child.constraints.minWidth + child.margin.width
            }
        }

        measurables.forEachIndexed { index, child ->
            if (parentDatas[index].run { weight <= 0 && fillMode != FillMode.MatchSibling }) {
                val placeable = child.measure(
                    Constraints.of(
                        0f,
                        (contentMaxWidth - usedWidth - minimumOccupiedWidth - child.margin.width).coerceAtLeast(0f),
                        if (parentDatas[index].fillMode == FillMode.MatchParent) contentMaxHeight - child.margin.height else 0f,
                        contentMaxHeight - child.margin.height
                    )
                )
                if (placeable.size.height + child.margin.height > maxChildHeight) maxChildHeight = placeable.size.height + child.margin.height
                usedWidth += placeable.size.width + child.margin.width
            }
        }

        measurables.forEachIndexed { index, child ->
            if (parentDatas[index].run { weight <= 0 && fillMode == FillMode.MatchSibling }) {
                val min = if (maxChildHeight > 0) maxChildHeight + child.margin.height else 0f
                val max = if (maxChildHeight > 0) maxChildHeight + child.margin.height else contentMaxHeight - child.margin.height
                val placeable = child.measure(
                    Constraints.of(
                        0f,
                        (contentMaxWidth - usedWidth - child.margin.width).coerceAtLeast(0f),
                        min, max
                    )
                )
                if (placeable.size.height + child.margin.height > maxChildHeight) maxChildHeight = placeable.size.height + child.margin.height
                usedWidth += placeable.size.width + child.margin.width
            }
        }

        val weightUnitWidth = if (totalWidget > 0) (contentMaxWidth - usedWidth) / totalWidget else 0f

        measurables.forEachIndexed { index, child ->
            if (parentDatas[index].run { weight > 0 && fillMode != FillMode.MatchSibling }) {
                val weight = parentDatas[index].weight
                val distributionWidth = ((weightUnitWidth * weight) - child.margin.width).coerceAtLeast(0f)
                val placeable = child.measure(
                    Constraints.of(
                        distributionWidth,
                        distributionWidth,
                        if (parentDatas[index].fillMode == FillMode.MatchParent) contentMaxHeight - child.margin.height else 0f,
                        contentMaxHeight - child.margin.height
                    )
                )
                if (placeable.size.height + child.margin.height > maxChildHeight) maxChildHeight = placeable.size.height + child.margin.height
                usedWidth += placeable.size.width + child.margin.width
            }
        }

        measurables.forEachIndexed { index, child ->
            if (parentDatas[index].run { weight > 0 && fillMode == FillMode.MatchSibling }) {
                val weight = parentDatas[index].weight
                val distributionWidth = ((weightUnitWidth * weight) - child.margin.width).coerceAtLeast(0f)
                val min = if (maxChildHeight > 0) maxChildHeight + child.margin.height else 0f
                val max = if (maxChildHeight > 0) maxChildHeight + child.margin.height else contentMaxHeight - child.margin.height
                val placeable = child.measure(
                    Constraints.of(distributionWidth, distributionWidth, min, max)
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

        val verticalOffsets = layoutables.mapIndexed { i, l -> contentBox.top + alignments[i].align(verticalSpace, l.wrappedHeight) }
        val horizontalOffsets = arrangement.arrange(
            horizontalSpace,
            layoutables.map {
                it.wrappedWidth
            })
            .map {
                contentBox.left + it
            }

        horizontalOffsets.zip(verticalOffsets).forEachIndexed { i, (x, y) ->
            val p = layoutables[i]
            val margin = p.margin
            p.placeAt(x + margin.left, y + margin.top, false)
        }
    }

}

