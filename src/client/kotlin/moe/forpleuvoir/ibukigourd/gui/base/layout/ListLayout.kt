package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.layout.util.FillMode
import moe.forpleuvoir.ibukigourd.gui.base.layout.util.FillMode.Companion.None

interface ListLayout : Layout {

    val spacing: Float

    val alignment: Alignment.Linear

    fun amount(): Float

}

interface WrappedListLayoutData {
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

    /**
     * 表示一个布尔值，用于确定是否解除对布局约束的限制。
     *
     * 当值为 `true` 时，表示在布局中可以解除某些特定的约束条件，这可能使子组件的位置或尺寸不再受严格限制。
     * 当值为 `false` 时，表示布局将严格按照定义的约束来计算和安排子组件。
     *
     * 这个变量通常用于需要动态调整布局行为的场景。
     */
    val unlockConstraint: Boolean
}

private const val UNLOCKED_MAX_CONSTRAINTS = 2333f

interface ColumnListLayout : ListLayout {

    data class WrappedColumnListLayoutData(
        override val fillMode: FillMode = None,
        override val alignment: Alignment.Horizontal? = null,
        override val unlockConstraint: Boolean = false,
    ) : WrappedListLayoutData {

        companion object : WrappedLayoutDataUtil<WrappedColumnListLayoutData> {
            override fun fromMeasurable(measurable: Measurable): WrappedColumnListLayoutData? =
                measurable.parentData as? WrappedColumnListLayoutData

            override fun default(): WrappedColumnListLayoutData = WrappedColumnListLayoutData()
        }

        fun getAlignment(layout: ColumnListLayout) = this.alignment ?: layout.alignment
    }

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

        fun contentMaxHeight(unconstrained: Boolean) =
            if (unconstrained) UNLOCKED_MAX_CONSTRAINTS
            else contentMaxHeight

        //所有元素的parentData
        val parentData = WrappedColumnListLayoutData.wrappedDatas(measurables)
        //使用的高度
        var usedHeight = spacing * measurables.lastIndex

        measurables
            .forEachIndexed { index, child ->
                if (parentData[index].fillMode != FillMode.MatchSibling) {
                    val placeable = child.measure(
                        Constraints.of(
                            if (parentData[index].fillMode == FillMode.MatchParent) contentMaxWidth - child.margin.width else 0f,
                            contentMaxWidth - child.margin.width,
                            0f,
                            (contentMaxHeight(parentData[index].unlockConstraint) - child.margin.height).coerceAtLeast(0f)
                        )
                    )

                    if (placeable.size.width + child.margin.width > maxChildWidth)
                        maxChildWidth = placeable.size.width + child.margin.width

                    usedHeight += placeable.size.height + child.margin.height
                }
            }

        //matchMaxSpace
        measurables
            .forEachIndexed { index, child ->
                if (parentData[index].fillMode == FillMode.MatchSibling) {

                    val min = if (maxChildWidth > 0) maxChildWidth - child.margin.width else 0f
                    val max = if (maxChildWidth > 0) maxChildWidth - child.margin.width else contentMaxWidth - child.margin.width
                    val placeable = child.measure(
                        Constraints.of(min, max, 0f, (contentMaxHeight(parentData[index].unlockConstraint) - child.margin.height).coerceAtLeast(0f))
                    )

                    if (placeable.size.width + child.margin.width > maxChildWidth)
                        maxChildWidth = placeable.size.width + child.margin.width

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
        val amount = this.amount()
        val alignments = WrappedColumnListLayoutData.wrappedDatas(layoutables).map { it.getAlignment(this) }
        Arrangement.spacedBy(spacing, Alignment.Top)
            //计算每一个组件的Y偏移
            .arrange(widget.contentWidth, layoutables.map { it.wrappedHeight })
            //映射每一个组件的本地Y位置
            .map { contentBox.top + it - amount }
            //与Y轴数据组合
            .zip(
                alignments
                    //计算每一个组件的X偏移
                    .mapIndexed { index, alignment -> alignment.align(contentBox.width, layoutables[index].wrappedWidth) }
                    //映射每一个组件的本地X位置
                    .map { contentBox.left + it }
            )
            .forEachIndexed { index, (y, x) ->
                val margin = layoutables[index].margin
                //放置每一个组件
                layoutables[index].placeAt(x + margin.left, y + margin.top, false)
            }
    }

}

interface RowListLayout : ListLayout {

    data class WrappedRowListLayoutData(
        override val fillMode: FillMode = None,
        override val alignment: Alignment.Vertical? = null,
        override val unlockConstraint: Boolean = false,
    ) : WrappedListLayoutData {

        companion object : WrappedLayoutDataUtil<WrappedRowListLayoutData> {
            override fun fromMeasurable(measurable: Measurable): WrappedRowListLayoutData? =
                measurable.parentData as? WrappedRowListLayoutData

            override fun default(): WrappedRowListLayoutData = WrappedRowListLayoutData()
        }

        fun getAlignment(layout: RowListLayout) = this.alignment ?: layout.alignment
    }

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

        fun contentMaxWidth(unconstrained: Boolean) =
            if (unconstrained) UNLOCKED_MAX_CONSTRAINTS
            else contentMaxWidth

        //所有元素的parentData
        val parentDatas = WrappedRowListLayoutData.wrappedDatas(measurables)
        //使用的宽度
        var usedWidth = spacing * measurables.lastIndex

        measurables
            .forEachIndexed { index, child ->
                if (parentDatas[index].fillMode != FillMode.MatchSibling) {

                    val placeable = child.measure(
                        Constraints.of(
                            0f,
                            (contentMaxWidth(parentDatas[index].unlockConstraint) - child.margin.width).coerceAtLeast(0f),
                            if (parentDatas[index].fillMode == FillMode.MatchParent) contentMaxHeight - child.margin.height else 0f,
                            contentMaxHeight - child.margin.height
                        )
                    )

                    if (placeable.size.height + child.margin.height > maxChildHeight)
                        maxChildHeight = placeable.size.height + child.margin.height

                    usedWidth += placeable.size.width + child.margin.width
                }
            }

        //matchMaxSpace
        measurables
            .forEachIndexed { index, child ->
                if (parentDatas[index].fillMode == FillMode.MatchSibling) {

                    val min = if (maxChildHeight > 0) maxChildHeight - child.margin.height else 0f
                    val max = if (maxChildHeight > 0) maxChildHeight - child.margin.height else contentMaxHeight - child.margin.height

                    val placeable = child.measure(
                        Constraints.of(
                            0f,
                            (contentMaxWidth(parentDatas[index].unlockConstraint) - child.margin.width).coerceAtLeast(0f),
                            min, max
                        )
                    )

                    if (placeable.size.height + child.margin.height > maxChildHeight)
                        maxChildHeight = placeable.size.height + child.margin.height

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
        val amount = this.amount()
        val alignments = WrappedRowListLayoutData.wrappedDatas(layoutables).map { it.getAlignment(this) }
        Arrangement.spacedBy(spacing, Alignment.Left)
            //计算每一个组件的X偏移
            .arrange(widget.contentWidth, layoutables.map { it.wrappedWidth })
            //映射每一个组件的本地X位置
            .map { contentBox.left + it - amount }
            //与Y轴数据组合
            .zip(
                alignments
                    //计算每一个组建的Y偏移
                    .mapIndexed { index, alignment -> alignment.align(contentBox.height, layoutables[index].wrappedHeight) }
                    //映射每一个组件的本地Y位置
                    .map { contentBox.top + it }
            )
            .forEachIndexed { index, (x, y) ->
                val margin = layoutables[index].margin
                //放置每一个组件
                layoutables[index].placeAt(x + margin.left, y + margin.top, false)
            }
    }

}