package moe.forpleuvoir.ibukigourd.gui.widget.layout

import moe.forpleuvoir.ibukigourd.gui.base.element.addDefaultLayer
import moe.forpleuvoir.ibukigourd.gui.base.event.MousePressEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseScrollEvent
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.scissor
import moe.forpleuvoir.ibukigourd.gui.base.layout.TableColumn
import moe.forpleuvoir.ibukigourd.gui.base.layout.TableColumn.Companion.extractColumns
import moe.forpleuvoir.ibukigourd.gui.base.layout.TableLayout
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Alignment
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.margin
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.padding
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.renderBackground
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.TableLayoutColumnScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.TableLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.Compose
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl
import moe.forpleuvoir.ibukigourd.gui.util.ScrollState
import moe.forpleuvoir.ibukigourd.gui.widget.Scroller
import moe.forpleuvoir.ibukigourd.gui.widget.theme.WidgetTheme
import moe.forpleuvoir.ibukigourd.gui.widget.theme.theme
import moe.forpleuvoir.nebula.common.util.primitive.sumOf
import net.minecraft.util.math.MathHelper

class TableWidget(
    override val alignment: Alignment,
    override val fixedHeader: Boolean,
    override val rowGap: Float,
    override val columnGap: Float,
    val scrollState: ScrollState,
) : WidgetContainerImpl(), TableLayout {

    init {
        scrollState.subscribe {
            layout()
        }
        addDefaultLayer(::renderChildren)
    }

    var enableScissor: Boolean = true

    override var hasHeader: Boolean = false
        private set

    override val columns: MutableList<TableColumn> = mutableListOf()

    override fun amount(): Float = scrollState.amount

    fun checkColumn() {
        val size = columns.first().size
        check(columns.all { it.size == size }) {
            "Column size mismatch detected: all columns must have the same number of elements."
        }
    }

    override fun recompose() {
        clearWidgetChildren()
        columns.clear()
        compose()
        remeasure()
    }

    override fun onMeasureCompletion() {
        val totalSpace = extractColumns(columns).map { it.maxOf { it.wrappedHeight } }.sumOf { it + columnGap } - columnGap
        scrollState {
            maxAmount = totalSpace - contentHeight
            barProportion = contentHeight / totalSpace
            amountStep = widgetChildren().minOf { it.transform.height }.coerceAtLeast(5f) / 2f
        }
        super<WidgetContainerImpl>.onMeasureCompletion()
    }

    override fun onMouseScrolling(event: MouseScrollEvent) {
        super.onMouseScrolling(event)
        event.tryUse(wasMouseOver).onSuccess { scrollState.scroll(event.verticalAmount) }
    }

    fun renderChildren(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        context.apply {
            val headHeight = if (hasHeader && fixedHeader) {
                columns.first().cachedCells.first()!!.second.height
            } else 0f

            val scissorBox = contentBox(true).trimEdges(top = MathHelper.ceil(headHeight).toFloat())

            if (enableScissor) {
                scissor(scissorBox) {
                    //渲染Cells
                    columns.forEachIndexed { rowIndex, tableColumn ->
                        tableColumn.cachedCells.forEachIndexed { columnIndex, cell ->
                            cell?.let { (widget, box) ->
                                if (!(hasHeader && columnIndex == 0)) {
                                    renderCells(widget, box, rowIndex, columnIndex, context, mouseX, mouseY, delta)
                                }
                            }
                        }
                    }
                    //渲染Header
                    if (hasHeader && !fixedHeader) columns.forEachIndexed { rowIndex, tableColumn ->
                        tableColumn.cachedCells.first()?.let { (widget, box) ->
                            renderHeaders(widget, box, rowIndex, context, mouseX, mouseY, delta)
                        }
                    }
                }
                if (hasHeader && fixedHeader) columns.forEachIndexed { rowIndex, tableColumn ->
                    tableColumn.cachedCells.first()?.let { (widget, box) ->
                        renderHeaders(widget, box, rowIndex, context, mouseX, mouseY, delta)
                    }
                }
            } else {
                //渲染Cells
                columns.forEachIndexed { rowIndex, tableColumn ->
                    tableColumn.cachedCells.forEachIndexed { columnIndex, cell ->
                        cell?.let { (widget, box) ->
                            if (!(hasHeader && columnIndex == 0)) {
                                renderCells(widget, box, rowIndex, columnIndex, context, mouseX, mouseY, delta)
                            }
                        }
                    }
                }
                //渲染Header
                if (hasHeader) {
                    columns.forEachIndexed { rowIndex, tableColumn ->
                        tableColumn.cachedCells.first()?.let { (widget, box) ->
                            renderHeaders(widget, box, rowIndex, context, mouseX, mouseY, delta)
                        }
                    }
                }
            }
        }
    }

    private fun renderHeaders(header: IGWidget, headerBox: Box, rowIndex: Int, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        renderHeader(header, headerBox, rowIndex, context, mouseX, mouseY, delta)
    }

    private fun renderCells(cell: IGWidget, cellBox: Box, rowIndex: Int, columnIndex: Int, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        renderCell(cell, cellBox, rowIndex, columnIndex, context, mouseX, mouseY, delta)
    }

    var renderHeader: (header: IGWidget, headerBox: Box, rowIndex: Int, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRenderHeader

    var renderCell: (cell: IGWidget, cellBox: Box, rowIndex: Int, columnIndex: Int, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRenderCell

    fun onRenderHeader(header: IGWidget, headerBox: Box, rowIndex: Int, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        if (!fixedHeader) {
            if ((header.transform.asWorldCoordinateBox intersectWith transform.asWorldCoordinateBox).exist) {
                header.clearActive()
                header.clearVisible()
            } else {
                header.active = false
                header.visible = false
            }
        }
        if (header.visible) header.render(context, mouseX, mouseY, delta)
    }

    fun onRenderCell(cell: IGWidget, cellBox: Box, rowIndex: Int, columnIndex: Int, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        val box = if (hasHeader && fixedHeader) {
            val height = columns.first().cachedCells.first()!!.second.height
            transform.asWorldCoordinateBox.copy(y = transform.asWorldCoordinateBox.y + height + padding.top)
        } else transform.asWorldCoordinateBox
        if ((cell.transform.asWorldCoordinateBox intersectWith box).exist) {
            cell.clearActive()
            cell.clearVisible()
        } else {
            cell.active = false
            cell.visible = false
        }

        if (cell.visible) cell.render(context, mouseX, mouseY, delta)
    }

    override fun onMousePress(event: MousePressEvent) {
        if (wasMouseOverContent) super.onMousePress(event)
    }

    class Scope<T>(
        val userData: Iterable<T>,
        private val tableWidget: TableWidget
    ) : GuiScope<TableWidget>, TableLayoutScope {
        override fun owner(): TableWidget = tableWidget

        class ColumnBuilder<out T>(private val header: TableLayoutColumnScope.() -> IGWidget, private val weight: Int, private val scope: Scope<T>) {

            fun Column(cell: TableLayoutColumnScope.(Int, T) -> IGWidget) {
                scope.owner().hasHeader = true
                val columnScope = TableLayoutColumnScope { scope.owner() }
                scope.tableWidget.columns += TableColumn(
                    listOf(columnScope.header()) + scope.userData.mapIndexed { index, entry -> columnScope.cell(index, entry) },
                    weight
                )
            }

            fun Column(cell: TableLayoutColumnScope.(T) -> IGWidget) {
                scope.owner().hasHeader = true
                val columnScope = TableLayoutColumnScope { scope.owner() }
                scope.tableWidget.columns += TableColumn(
                    listOf(columnScope.header()) + scope.userData.map { entry -> columnScope.cell(entry) },
                    weight
                )
            }

        }

        /**
         * 用于创建一个列的构建器，并将其与指定的表头小部件相关联。
         * 在一个表中,如果有一列使用了列头,那么所有列都需要声明列头
         *
         * @param header 一个用于定义列头组件的函数，它在特定的表布局列作用域中定义如何构造列头部件，返回一个实现了IGWidget的组件实例。
         * @return 返回一个ColumnBuilder实例，该实例绑定了提供的表头生成逻辑和相关的作用域。
         */
        fun Header(weight: Int = 0, header: TableLayoutColumnScope.() -> IGWidget): ColumnBuilder<T> {
            return ColumnBuilder(header, weight, this)
        }

        fun ColumnBuilder(weight: Int = 0, columnEntry: TableLayoutColumnScope.(Int, T) -> IGWidget) {
            val scope = TableLayoutColumnScope { owner() }
            tableWidget.columns += TableColumn(
                userData.mapIndexed { index, entry -> scope.columnEntry(index, entry) },
                weight
            )
        }

        fun ColumnBuilder(weight: Int = 0, columnEntry: TableLayoutColumnScope.(T) -> IGWidget) {
            val scope = TableLayoutColumnScope { owner() }
            tableWidget.columns += TableColumn(
                userData.map { entry -> scope.columnEntry(entry) },
                weight
            )
        }

        fun enableScissor() {
            owner().enableScissor = true
        }

        fun disableScissor() {
            owner().enableScissor = false
        }

        fun onRenderHeader(render: TableWidget.(header: IGWidget, headerBox: Box, rowIndex: Int, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit) {
            owner().renderHeader = { header, headerBox, rowIndex, context, mouseX, mouseY, delta ->
                this.owner().render(header, headerBox, rowIndex, context, mouseX, mouseY, delta)
            }
        }

        fun onRenderCell(render: TableWidget.(cell: IGWidget, cellBox: Box, rowIndex: Int, columnIndex: Int, context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit) {
            owner().renderCell = { cell, cellBox, rowIndex, columnIndex, context, mouseX, mouseY, delta ->
                this.owner().render(cell, cellBox, rowIndex, columnIndex, context, mouseX, mouseY, delta)
            }
        }

    }
}

typealias TableScope<T> = TableWidget.Scope<T>

fun <T> WidgetContainerScope.Table(
    userData: Iterable<T>,
    defaultAlignment: Alignment = Alignment.Center,
    fixedHeader: Boolean = true,
    rowGap: Float = 1f,
    columnGap: Float = 1f,
    scrollState: ScrollState = ScrollState(),
    modifier: Modifier = Modifier,
    scope: TableScope<T>.() -> Unit
) = addWidgetChild(TableWidget(defaultAlignment, fixedHeader, rowGap, columnGap, scrollState)) {
    modifier.foldInApply()
    TableScope(userData, this).Compose {
        scope()
        checkColumn()
    }
}

fun <T> WidgetContainerScope.TableWrapped(
    userData: Iterable<T>,
    modifier: Modifier = Modifier,
    defaultAlignment: Alignment = Alignment.Center,
    fixedHeader: Boolean = true,
    rowGap: Float = 1f,
    columnGap: Float = 1f,
    scrollerModifier: RowScope.() -> Modifier = { Modifier },
    scrollState: ScrollState = ScrollState(),
    barThickness: Float = 9f,
    tableModifier: RowScope.() -> Modifier = { Modifier },
    scope: TableScope<T>.() -> Unit
) = Row(
    modifier = Modifier
        .renderBackground { context, _, _, _ ->
            context.batchRenderTextureColored {
                pushWidgetTexture(transform, theme(WidgetTheme.ListLayout))
            }
        }
        .padding(3).then(modifier),
    horizontalArrangement = Arrangement.SpaceBetween,
) {
    Table(
        userData = userData,
        defaultAlignment = defaultAlignment,
        fixedHeader = fixedHeader,
        rowGap = rowGap,
        columnGap = columnGap,
        scrollState = scrollState,
        modifier = tableModifier(),
        scope = scope
    )
    Scroller(
        scrollState = scrollState,
        orientation = Orientation.Vertical,
        modifier = Modifier
            .matchSibling()
            .width(barThickness)
            .margin(top = 1f, left = 1f) then scrollerModifier()
    )
}