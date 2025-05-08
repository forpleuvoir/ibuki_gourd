package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.TableColumn.Companion.extractColumns
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Constraints
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.math.plus

interface TableLayout : Layout {

    companion object {

        private const val UNLOCKED_MAX_CONSTRAINTS = 2333f

    }

    val alignment: Alignment

    val fixedHeader: Boolean

    val hasHeader: Boolean

    val columns: List<TableColumn>

    val rowGap: Float

    val columnGap: Float

    fun amount(): Float

    @Deprecated("", replaceWith = ReplaceWith("measureColumns(columns, constraints): Placeable"))
    override fun measureChildren(measurables: List<Measurable>, constraints: Constraints): Placeable = widget

    @Deprecated("", replaceWith = ReplaceWith("layoutColumn(columns)"))
    override fun layout(layoutables: List<Layoutable>) = Unit

    override fun measure(constraints: Constraints): Placeable {
        return measureColumns(columns, this.constraints.constraintAs(constraints))
    }

    override fun layout() {
        val layoutables = layoutableChildren()
        if (layoutables.isEmpty()) return
        layoutColumn(columns)
        layoutables.filter { it is Layout }.forEach { (it as Layout).layout() }
        layoutCompletion()
    }

    fun measureColumns(columns: List<TableColumn>, constraints: Constraints): Placeable {
        //行大小,一行有多少组件
        val columnCount = columns.size
        //列大小,一列有多少组件
        val rowCount = columns.firstOrNull()?.size ?: 0
        if (rowCount < 1 || columnCount < 1) return widget
        //行间距
        val totalGapWidth = rowGap * (columnCount - 1)
        //列间距
        val totalGapHeight = columnGap * (rowCount - 1)

        //垂直布局 宽度固定
        val (minWidth, maxWidth, minHeight, maxHeight) = this.constraints.constraintAs(constraints)
        //所有子元素的最大宽度限制固定
        val contentMaxWidth = (maxWidth - widget.padding.width - totalGapWidth).coerceAtLeast(0f)
        val defaultMaxHeight = (maxHeight - widget.padding.height).coerceAtLeast(0f)

        fun contentMaxHeight(unconstrained: Boolean) =
            if (unconstrained) UNLOCKED_MAX_CONSTRAINTS
            else defaultMaxHeight

        //每一列的最大宽度
        val maxColumnWidth = FloatArray(columnCount)
        //每一行的最大高度
        val maxRowHeight = FloatArray(rowCount)
        //已使用的宽度
        var usedWidth = 0f
        //父数据
        val parentData = columns.map { WrappedTableColumnEntryData.wrappedData(it.column) }
        //总权重
        val totalWidget = columns.sumOf { it.weight }

        //优先测量无权重的列
        columns.forEachIndexed { columnIndex, tableColumn ->
            if (tableColumn.weight <= 0) {
                tableColumn.column.forEachIndexed { rowIndex, layoutable ->
                    val parentData = parentData[columnIndex][rowIndex]
                    val placeable = layoutable.measure(
                        Constraints.of(
                            0f, contentMaxWidth - usedWidth,
                            0f, (contentMaxHeight(parentData.unlockConstraint) - layoutable.margin.height).coerceAtLeast(0f)
                        )
                    )
                    if (placeable.wrappedWidth > maxColumnWidth[columnIndex]) maxColumnWidth[columnIndex] = placeable.wrappedWidth
                    if (placeable.wrappedHeight > maxRowHeight[rowIndex]) maxRowHeight[rowIndex] = placeable.wrappedHeight
                }
                usedWidth += maxColumnWidth[columnIndex]
            }
        }

        //单位权重所占的高度
        val weightUnitWidth = if (totalWidget > 0) (contentMaxWidth - usedWidth) / totalWidget else 0f

        if (totalWidget != 0)
            columns.forEachIndexed { columnIndex, tableColumn ->
                if (tableColumn.weight > 0) {
                    val weight = tableColumn.weight
                    tableColumn.column.forEachIndexed { rowIndex, layoutable ->
                        val distributionWidth = ((weightUnitWidth * weight) - layoutable.margin.width).coerceAtLeast(0f)
                        val parentData = parentData[columnIndex][rowIndex]
                        val placeable = layoutable.measure(
                            Constraints.of(
                                distributionWidth, distributionWidth,
                                0f, (contentMaxHeight(parentData.unlockConstraint) - layoutable.margin.height).coerceAtLeast(0f)
                            )
                        )
                        if (placeable.wrappedWidth > maxColumnWidth[columnIndex]) maxColumnWidth[columnIndex] = placeable.wrappedWidth
                        if (placeable.wrappedHeight > maxRowHeight[rowIndex]) maxRowHeight[rowIndex] = placeable.wrappedHeight
                    }
                    usedWidth += maxColumnWidth[columnIndex]
                }
            }

        usedWidth += widget.padding.width + totalGapWidth
        val totalHeight = maxRowHeight.sum() + widget.padding.height + totalGapHeight
        widget.transform.set(usedWidth.coerceIn(minWidth, maxWidth), totalHeight.coerceIn(minHeight, maxHeight))
        return widget
    }

    fun layoutColumn(columns: List<TableColumn>) {
        val contentBox = widget.contentBox(false)
        val amount = this.amount()
        val alignments = columns.map { WrappedTableColumnEntryData.wrappedData(it.column).map { it.getAlignment(this) } }

        val heights = extractColumns(columns).map { it.maxOf { it.wrappedHeight } }
        val widths = columns.map { it.getWrappedWidth() }
        var x = contentBox.x
        columns.forEachIndexed { rowIndex, tableColumn ->

            var y = contentBox.y - amount

            tableColumn.column.forEachIndexed { columnIndex, layoutable ->

                val baseOffset = Vector2f(
                    x + layoutable.margin.left,
                    if (hasHeader && fixedHeader && columnIndex == 0) contentBox.y else y + layoutable.margin.top
                )

                val size = Size(widths[rowIndex], heights[columnIndex])

                val alignment = alignments[rowIndex][columnIndex]

                val offset = alignment.align(size, layoutable.size)

                layoutable.placeAt(baseOffset + offset, false)

                val cell = layoutable to Box(baseOffset, size)
                tableColumn.cachedCells[columnIndex] = cell

                y += heights[columnIndex] + columnGap
            }

            x += widths[rowIndex] + rowGap
        }

    }

}

data class TableColumn(
    val column: List<IGWidget>,
    val weight: Int = 0,
    val size: Int = column.size,
) {

    val cachedCells = arrayOfNulls<Pair<IGWidget, Box>>(size)

    companion object {
        fun extractColumns(tableColumns: List<TableColumn>): List<List<IGWidget>> {
            if (tableColumns.isEmpty()) return emptyList()
            val rowCount = tableColumns.first().column.size
            val result = List(rowCount) { mutableListOf<IGWidget>() }
            for (tableColumn in tableColumns) {
                for (i in tableColumn.column.indices) {
                    result[i].add(tableColumn.column[i])
                }
            }
            return result.map { it }
        }

    }

    fun getWrappedWidth() = column.maxOf { it.wrappedWidth }

}

data class WrappedTableColumnEntryData(
    val alignment: Alignment? = null,
    val unlockConstraint: Boolean = false,
) {
    companion object : WrappedLayoutDataUtil<WrappedTableColumnEntryData> {
        override fun fromMeasurable(measurable: Measurable): WrappedTableColumnEntryData? =
            measurable.parentData as? WrappedTableColumnEntryData

        override fun default() = WrappedTableColumnEntryData()

    }

    fun getAlignment(layout: TableLayout) = this.alignment ?: layout.alignment
}