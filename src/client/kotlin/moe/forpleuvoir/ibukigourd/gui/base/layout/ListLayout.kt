package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.peek
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface ListLayout : Layout {

    @Suppress("DuplicatedCode", "LocalVariableName")
    companion object {

        private fun ListLayout.measureVertical(measurables: List<Measurable>, constraints: Constraints): Placeable {
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
            val parentDatas = measurables.map { WrappedListLayoutData.getOrDefault(it) }
            //使用的高度
            var usedHeight = 0f

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
                placeables[index] = placeable
            }

            usedHeight += widget.padding.height + spacing * measurables.lastIndex
            maxChildWidth += widget.padding.width
            return applyResult(maxChildWidth.coerceIn(_minWidth, _maxWidth), usedHeight.coerceIn(_minHeight, _maxHeight)) {
                layout(placeables.map { it!! }, parentDatas)
            }
        }

        private fun ListLayout.measureHorizontal(measurables: List<Measurable>, constraints: Constraints): Placeable {
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
            val parentDatas = measurables.map { WrappedListLayoutData.getOrDefault(it) }
            //使用的宽度
            var usedWidth = 0f

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
                placeables[index] = placeable
            }

            usedWidth += widget.padding.width + spacing * measurables.lastIndex
            maxChildHeight += widget.padding.height
            return applyResult(usedWidth.coerceIn(_minWidth, _maxWidth), maxChildHeight.coerceIn(_minHeight, _maxHeight)) {
                layout(placeables.map { it!! }, parentDatas)
            }
        }

        private fun ListLayout.layoutVertical(placeables: List<Placeable>, parentDatas: List<WrappedListLayoutData>) {
            var y = widget.padding.top - amount()
            placeables.forEachIndexed { index, child ->
                val x = when (parentDatas[index].gravity) {
                    Gravity.Start  -> widget.padding.left + child.margin.left
                    Gravity.Center -> widget.transform.halfWidth - (child.margin.left + child.size.halfWidth)
                    Gravity.End    -> widget.transform.width - widget.padding.right - child.size.width - child.margin.right
                }
                child.placeAt(x, y, false)
                y += child.wrappedSize.height + spacing
            }
        }

        private fun ListLayout.layoutHorizontal(placeables: List<Placeable>, parentDatas: List<WrappedListLayoutData>) {
            var x = widget.padding.top - amount()
            placeables.forEachIndexed { index, child ->
                val y = when (parentDatas[index].gravity) {
                    Gravity.Start  -> widget.padding.top + child.margin.top
                    Gravity.Center -> widget.transform.halfHeight - (child.margin.top + child.size.halfHeight)
                    Gravity.End    -> widget.transform.height - widget.padding.bottom - child.size.height - child.margin.bottom
                }
                child.placeAt(x, y, false)
                x += child.wrappedSize.width + spacing
            }
        }


        @OptIn(ExperimentalContracts::class)
        private inline fun ListLayout.applyResult(width: Float, height: Float, block: () -> Unit): Placeable {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }
            widget.transform.set(width, height)
            block()
            return widget
        }
    }

    val orientation: Orientation

    val spacing: Float

    val amount: () -> Float

    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable =
        orientation.peek(
            { this.measureVertical(measurables, constraints) },
            { this.measureHorizontal(measurables, constraints) }
        )

    override fun layout(placeables: List<Placeable>, parentDatas: List<Any?>) {
        if (placeables.isEmpty()) return
        val datas = parentDatas.map { it as WrappedListLayoutData }
        orientation.peek(
            { this.layoutVertical(placeables, datas) },
            { this.layoutHorizontal(placeables, datas) }
        )
    }


}

data class WrappedListLayoutData(
    val fill: Boolean = false,
    var gravity: Gravity = Gravity.Center
) {

    companion object {

        private val default = WrappedListLayoutData()

        fun fromMeasurable(measurable: Measurable): WrappedListLayoutData? {
            return measurable.parentData as? WrappedListLayoutData
        }

        fun getOrDefault(measurable: Measurable, default: WrappedListLayoutData = this.default): WrappedListLayoutData {
            return fromMeasurable(measurable) ?: default
        }

    }

}