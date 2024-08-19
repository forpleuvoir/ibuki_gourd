package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable

interface ListLayout : Layout {

    val spacing: Float

    val alignment: Alignment.Linear

    fun amount(): Float

}

interface WrappedListLayoutData {
    val fill: Boolean
    val alignment: Alignment.Linear?

}

interface VerticalListLayout : ListLayout {

    data class WrappedVerticalListLayoutData(
        override val fill: Boolean = true,
        override val alignment: Alignment.Horizontal? = null
    ) : WrappedListLayoutData {

        companion object : WrappedLayoutDataUtil<WrappedVerticalListLayoutData> {
            override fun default(): WrappedVerticalListLayoutData = WrappedVerticalListLayoutData()
        }

        fun getAlignment(layout: VerticalListLayout) = this.alignment ?: layout.alignment
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
        //所有元素的parentData
        val parentDatas = WrappedVerticalListLayoutData.wrappedDatas(measurables)
        //使用的高度
        var usedHeight = spacing * measurables.lastIndex

        measurables.forEachIndexed { index, child ->
            val placeable = child.measure(
                Constraints.of(
                    if (parentDatas[index].fill) contentMaxWidth - child.margin.width else 0f,
                    contentMaxWidth - child.margin.width,
                    0f,
                    (contentMaxHeight - child.margin.height).coerceAtLeast(0f)
                )
            )
            if (placeable.size.width + child.margin.width > maxChildWidth) maxChildWidth = placeable.size.width + child.margin.width
            usedHeight += placeable.size.height + child.margin.height
        }

        usedHeight += widget.padding.height
        maxChildWidth += widget.padding.width
        widget.transform.set(maxChildWidth.coerceIn(minWidth, maxWidth), usedHeight.coerceIn(minHeight, maxHeight))
        return widget
    }

    override fun layout(layoutables: List<Layoutable>) {
        val contentBox = widget.contentBox(false)
        val amount = this.amount()
        val alignments = WrappedVerticalListLayoutData.wrappedDatas(layoutables).map { it.getAlignment(this) }
        Arrangement.spacedBy(spacing, Alignment.Top)
            //计算每一个组件的Y偏移
            .arrange(widget.contentWidth, layoutables.map { it.wrappedHeight })
            //映射每一个组件的本地Y位置
            .map { amount + contentBox.top + it }
            //与Y轴数据组合
            .zip(
                alignments
                    //计算每一个组建的X偏移
                    .mapIndexed { index, alignment -> alignment.align(layoutables[index].wrappedWidth, contentBox.width) }
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

interface HorizontalListLayout : ListLayout {

    data class WrappedHorizontalListLayoutData(
        override val fill: Boolean = true,
        override val alignment: Alignment.Vertical? = null
    ) : WrappedListLayoutData {

        companion object : WrappedLayoutDataUtil<WrappedHorizontalListLayoutData> {
            override fun default(): WrappedHorizontalListLayoutData = WrappedHorizontalListLayoutData()
        }

        fun getAlignment(layout: HorizontalListLayout) = this.alignment ?: layout.alignment
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
        //所有元素的parentData
        val parentDatas = WrappedHorizontalListLayoutData.wrappedDatas(measurables)
        //使用的宽度
        var usedWidth = spacing * measurables.lastIndex

        measurables.forEachIndexed { index, child ->
            val placeable = child.measure(
                Constraints.of(
                    0f,
                    (contentMaxWidth - child.margin.width).coerceAtLeast(0f),
                    if (parentDatas[index].fill) contentMaxHeight - child.margin.height else 0f,
                    contentMaxHeight - child.margin.height
                )
            )
            if (placeable.size.height + child.margin.height > maxChildHeight) maxChildHeight = placeable.size.height + child.margin.height
            usedWidth += placeable.size.width + child.margin.width
        }

        usedWidth += widget.padding.width
        maxChildHeight += widget.padding.height
        widget.transform.set(usedWidth.coerceIn(minWidth, maxWidth), maxChildHeight.coerceIn(minHeight, maxHeight))
        return widget
    }

    override fun layout(layoutables: List<Layoutable>) {
        val contentBox = widget.contentBox(false)
        val amount = this.amount()
        val alignments = WrappedHorizontalListLayoutData.wrappedDatas(layoutables).map { it.getAlignment(this) }
        Arrangement.spacedBy(spacing, Alignment.Left)
            //计算每一个组件的X偏移
            .arrange(widget.contentWidth, layoutables.map { it.wrappedWidth })
            //映射每一个组件的本地X位置
            .map { amount + contentBox.left + it }
            //与Y轴数据组合
            .zip(
                alignments
                    //计算每一个组建的Y偏移
                    .mapIndexed { index, alignment -> alignment.align(layoutables[index].wrappedHeight, contentBox.height) }
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