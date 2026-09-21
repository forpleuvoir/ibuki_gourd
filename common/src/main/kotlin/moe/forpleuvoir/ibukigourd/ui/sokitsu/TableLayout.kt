package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * 表格 DSL 标记：禁止在作用域外混用其它接收者（同 `RadioButtonGroupDsl` 的做法）。
 */
@DslMarker
annotation class TableDsl

/**
 * 列宽。
 *
 * 只有固定宽度与权重两种 —— 二者都不需要跨行测量，因此在 [TableLayout] 与 [LazyTableLayout]
 * 两种内核下表现完全一致。"按内容撑开"的列宽要求知道该列**所有**单元格的最大宽度，
 * 惰性列表结构上拿不到（行没被组合），故不提供。
 */
sealed interface TableColumnWidth {

    /**
     * 固定宽度。**硬约束**：内容比列宽宽时会被压扁（而不是把列撑开），
     * 因此图标按钮这类尺寸固定的内容，列宽要按它的自然尺寸给
     * （例如 `max(IconButtonDefaults.minSize.width, 图标尺寸 + 左右内边距)`）。
     */
    @JvmInline
    value class Fixed(val width: Dp) : TableColumnWidth

    /** 按权重分配行内剩余宽度（同 `RowScope.weight` 语义）。 */
    @JvmInline
    value class Fraction(val weight: Float) : TableColumnWidth
}

/**
 * 单元格作用域。
 *
 * 继承 [BoxScope]：单元格内容可用 `Modifier.align(...)` 覆盖列级默认对齐
 * （见 [TableLayoutScope.column] 的 `alignment`）。
 *
 * [dragHandle] 只在 [LazyTableLayout] 开启了行拖拽（`onRowMove` 非 null）且当前行可拖时生效，
 * 其余场合是 no-op。
 */
interface TableCellScope : BoxScope {

    /** 当前行是否正在被拖拽；未开启行拖拽时恒为 `false`。 */
    val isRowDragging: Boolean

    /**
     * 把该修饰符所在的节点变成行拖拽手柄（惯例用法：放在首个列的单元格里，配 `DragHandle`）。
     *
     * 只对当前行的拖拽生效；未开启行拖拽、或当前行不可拖（[TableLayoutScope.spanItem]）时原样返回。
     *
     * @param enabled 手柄是否响应拖拽
     */
    fun Modifier.dragHandle(enabled: Boolean = true): Modifier
}

/**
 * 表格定义作用域：**列（表头 + 宽度 + 单元格）与行数据都写在这里**，组件参数里不出现数据。
 *
 * 行的顺序 = 登记顺序：[TableLayoutScope.spanItem] 与 [TableLayoutScope.rows] 按调用先后排列；
 * [TableLayoutScope.rows] 用的是**它之前**已声明的列，因此 `column(...)` 必须写在 `rows(...)` 前面。
 */
@TableDsl
interface TableLayoutScope<T> {

    /** 固定列宽。 */
    fun fixed(width: Dp): TableColumnWidth = TableColumnWidth.Fixed(width)

    /** 按权重分配剩余宽度的列宽。 */
    fun weight(weight: Float = 1f): TableColumnWidth = TableColumnWidth.Fraction(weight)

    /**
     * 声明一列：宽度、对齐与表头都和它绑在一起。
     *
     * @param width 列宽
     * @param alignment 单元格内容的默认对齐；单元格内可用 `Modifier.align(...)` 覆盖
     * @param header 表头单元格；**所有列都不声明表头 = 整表没有表头行**
     * @param cell 单元格内容；`index` 为该元素在本次 `rows(...)` 里的下标
     */
    fun column(
        width: TableColumnWidth,
        alignment: Alignment = Alignment.Center,
        header: (@Composable TableCellScope.() -> Unit)? = null,
        cell: @Composable TableCellScope.(index: Int, item: T) -> Unit,
    )

    /**
     * 声明一个不属于任何列的整行（分组标题、空状态、分隔行等），占满表宽、不参与列宽分配、
     * 也不参与行拖拽。
     *
     * @param key lazy 列表用的稳定 key；不传时按登记序号生成
     */
    fun spanItem(key: Any? = null, content: @Composable () -> Unit)

    /**
     * 用当前已声明的列渲染 [items] 的每一行。
     *
     * @param items 数据源；本组件只读它，不持有所有权
     * @param key 行 key，须全表唯一；[LazyTableLayout] 开启行拖拽时**必填**（拖拽库按 key 追踪行身份，
     *   未提供时直接报错），同时也是惰性列表的 item key 与滚动位置锚点
     */
    fun rows(items: List<T>, key: ((item: T) -> Any)? = null)
}

/**
 * 表格（eager）：每行都是一个 [Row]，每格按列宽挂 `width` / `weight`，
 * 因此**列对齐是结构性的**，不依赖任何跨行测量。
 *
 * 表头（[TableLayoutScope.column] 的 `header`）：
 * - [fixedHeader] 为 true 且至少一列声明了表头 → 表头排在滚动区**之外**、恒在顶部，
 *   表体由本组件自持 [scrollState]（**要求外层给出有界高度**，例如 `fillMaxSize()` 或 `height(...)`）；
 * - 否则表头作为表体第一行随之滚动，本组件**不自持滚动**（滚动由调用方挂在 [modifier] 上，
 *   例如 `Modifier.verticalScroll(...)`）。
 *
 * 所有行都会被组合：**任何一次数据变化（增删、重排）都要重组并重排整张表**，几百行会明显掉帧，
 * 因此它只适合几十行以内的表格；行数大或需要拖拽排序用 [LazyTableLayout]（它每次数据变化只更新
 * 可见行）。
 *
 * @param modifier 作用于整表
 * @param fixedHeader 是否把表头钉在顶部（见上）
 * @param rowGap 行间距
 * @param columnGap 列间距
 * @param scrollState 固定表头时表体所用的滚动状态，可直接交给 `VerticalOverlayScroller`；不固定表头时不使用
 * @param rowModifier 逐行附加的修饰（悬停底色、选中态等），参数为该行在所属 `rows(...)` 里的下标；
 *   是 composable 函数类型，可以直接读悬停 / 动画状态
 * @param headerModifier 表头行的附加修饰；固定表头时表头浮在内容之上，给它一个**不透明底色**
 *   才能盖住从下方滚过的行
 * @param content 列与行的声明
 */
@Composable
fun <T> TableLayout(
    modifier: Modifier = Modifier,
    fixedHeader: Boolean = true,
    rowGap: Dp = TableLayoutDefaults.RowGap,
    columnGap: Dp = TableLayoutDefaults.ColumnGap,
    scrollState: ScrollState = rememberScrollState(),
    rowModifier: @Composable (index: Int) -> Modifier = { Modifier },
    headerModifier: Modifier = Modifier,
    content: TableLayoutScope<T>.() -> Unit,
) {
    val description = TableDescriptionBuilder<T>().apply(content).build(requireItemKey = false)
    val pinnedHeader = fixedHeader && description.hasHeader

    if (pinnedHeader) {
        Column(modifier = modifier) {
            TableHeaderRow(description.columns, columnGap, headerModifier)
            Spacer(Modifier.height(rowGap))
            Box(Modifier.weight(1f).verticalScroll(scrollState)) {
                Column(verticalArrangement = Arrangement.spacedBy(rowGap)) {
                    TableBodyContent(description, columnGap, rowModifier)
                }
            }
        }
    } else {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(rowGap)) {
            if (description.hasHeader) TableHeaderRow(description.columns, columnGap, headerModifier)
            TableBodyContent(description, columnGap, rowModifier)
        }
    }
}

/**
 * 表格（lazy）：行是 `LazyColumn` 的 item，只有可见行被组合；列对齐与 [TableLayout] 同构
 * （每行一个 [Row]、每格挂 `width` / `weight`），因此惰性化不会让列错位。
 *
 * 表头：[fixedHeader] 为 true 时走 `LazyColumn` 的 `stickyHeader`（滚动中恒在顶部），
 * 否则是普通的首个 item（随内容滚走）。
 *
 * 行拖拽（[onRowMove]）：内部用 `reorderable` 的 `ReorderableItem` 包装每一行，
 * 拖拽手柄由单元格作用域的 `Modifier.dragHandle()` 提供（见 [TableCellScope.dragHandle]）。
 * 表头与 [TableLayoutScope.spanItem] 不参与拖拽（后者会随行让位，但本身不可拖）。
 *
 * @param modifier 作用于整表
 * @param fixedHeader 是否把表头钉在顶部
 * @param rowGap 行间距
 * @param columnGap 列间距
 * @param listState 列表状态，可直接交给 `VerticalOverlayScroller`
 * @param rowModifier 逐行附加的修饰，参数为该行在所属 `rows(...)` 里的下标（同上，可读 composition 状态）
 * @param headerModifier 表头行的附加修饰；`fixedHeader` 走 `stickyHeader` 时表头钉在视口顶端、
 *   内容从它下方滚过，给一个**不透明底色**才能盖住下面的行
 * @param onRowMove 行拖拽落点回调，参数是**可拖拽行的序号**（跳过表头与 `spanItem`，按
 *   `rows(...)` 的登记顺序连续编号）；为 null 时完全不接入拖拽。开启后每个 `rows(...)` 必须给 `key`
 * @param content 列与行的声明
 */
@Composable
fun <T> LazyTableLayout(
    modifier: Modifier = Modifier,
    fixedHeader: Boolean = true,
    rowGap: Dp = TableLayoutDefaults.RowGap,
    columnGap: Dp = TableLayoutDefaults.ColumnGap,
    listState: LazyListState = rememberLazyListState(),
    rowModifier: @Composable (index: Int) -> Modifier = { Modifier },
    headerModifier: Modifier = Modifier,
    onRowMove: ((from: Int, to: Int) -> Unit)? = null,
    content: TableLayoutScope<T>.() -> Unit,
) {
    val description = TableDescriptionBuilder<T>().apply(content).build(requireItemKey = onRowMove != null)
    val reorderState = if (onRowMove != null) {
        rememberReorderableLazyListState(listState) { from, to ->
            // 惰性 item 序号里含着表头与 spanItem，不能当行号用；按块起点换算（O(块数)，不随行数增长）
            val blocks = description.rowBlocks
            val fromOrdinal = blocks.firstNotNullOfOrNull { it.ordinalAt(from.index) }
            val toOrdinal = blocks.firstNotNullOfOrNull { it.ordinalAt(to.index) }
            if (fromOrdinal != null && toOrdinal != null && fromOrdinal != toOrdinal) {
                onRowMove(fromOrdinal, toOrdinal)
            }
        }
    } else {
        null
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(rowGap),
    ) {
        if (description.hasHeader) {
            if (fixedHeader) {
                stickyHeader(key = TableHeaderKey) {
                    TableHeaderRow(description.columns, columnGap, headerModifier)
                }
            } else {
                item(key = TableHeaderKey) { TableHeaderRow(description.columns, columnGap, headerModifier) }
            }
        }

        description.entries.forEachIndexed { entryIndex, entry ->
            when (entry) {
                is TableSpanEntry -> {
                    val spanKey = entry.key ?: "$TableSpanKeyPrefix$entryIndex"
                    item(key = spanKey) {
                        if (reorderState != null) {
                            ReorderableTableItem(reorderState, spanKey, enabled = false) { entry.content() }
                        } else {
                            entry.content()
                        }
                    }
                }

                is TableRowsEntry -> {
                    val columns = entry.columns
                    val itemKey = entry.itemKey
                    val lazyKeys: ((index: Int, item: T) -> Any)? =
                        itemKey?.let { keyOf -> { _, item -> keyOf(item) } }
                    itemsIndexed(items = entry.items, key = lazyKeys) { index, item ->
                        val row: @Composable (Boolean) -> Unit = { dragging ->
                            TableRowContent(
                                columns = columns,
                                index = index,
                                item = item,
                                columnGap = columnGap,
                                modifier = rowModifier(index),
                                isRowDragging = dragging,
                            )
                        }
                        // 拖拽开启时 itemKey 必非 null（见 TableDescriptionBuilder.build 的校验）
                        if (reorderState != null) {
                            ReorderableTableItem(reorderState, itemKey!!.invoke(item), enabled = true, content = row)
                        } else {
                            row(false)
                        }
                    }
                }
            }
        }
    }
}

/** 表头在惰性列表里的 key（与行 key 同处一个命名空间，取带前缀的字符串避免碰撞）。 */
private const val TableHeaderKey = "ibukigourd:table/header"

/** [TableLayoutScope.spanItem] 未给 key 时的 key 前缀。 */
private const val TableSpanKeyPrefix = "ibukigourd:table/span/"

/**
 * 一列的定义。
 *
 * @param width 列宽
 * @param alignment 单元格默认对齐
 * @param header 表头单元格；null = 本列没有表头内容
 * @param cell 单元格内容
 */
private class TableColumnSpec<T>(
    val width: TableColumnWidth,
    val alignment: Alignment,
    val header: (@Composable TableCellScope.() -> Unit)?,
    val cell: @Composable TableCellScope.(index: Int, item: T) -> Unit,
)

/** 表体的一行级条目：要么是整行内容，要么是一批用列渲染的数据行。 */
private sealed interface TableBodyEntry<T>

/** 不占列的整行。 */
private class TableSpanEntry<T>(
    val key: Any?,
    val content: @Composable () -> Unit,
) : TableBodyEntry<T>

/** 一批数据行：用**登记时刻**的列快照渲染，因此列声明写在后面的不会影响它。 */
private class TableRowsEntry<T>(
    val items: List<T>,
    val itemKey: ((item: T) -> Any)?,
    val columns: List<TableColumnSpec<T>>,
) : TableBodyEntry<T>

/**
 * 表定义（每次组合重建）：列、行级条目，以及各 `rows(...)` 块在惰性 item 序号空间里的位置。
 *
 * @param rowBlocks 可拖拽行（即 `rows(...)` 的行）的位置表；拖拽回调把它们换算成行序号
 */
private class TableDescription<T>(
    val columns: List<TableColumnSpec<T>>,
    val entries: List<TableBodyEntry<T>>,
    val rowBlocks: List<TableRowBlock>,
) {
    /** 是否至少有一列声明了表头内容。 */
    val hasHeader: Boolean get() = columns.any { it.header != null }
}

/**
 * 一个 `rows(...)` 块在惰性 item 序号空间里的位置：块内行号连续，一次减法就能换算。
 *
 * 惰性 item 序号里混着表头与 [TableLayoutScope.spanItem]，逐个 key 建映射的代价是 O(行数)，
 * 而拖拽每越过一行就会调用一次回调 —— 500 行时足以看到掉帧；块的起点是 O(块数) 的固定信息。
 *
 * @param firstItemIndex 该块首个 item 的惰性序号（表头与它前面的 `spanItem` 各占 1）
 * @param firstOrdinal 该块首个可拖拽行的序号（可拖拽行按登记顺序连续编号）
 * @param itemCount 该块的 item 数（等于它的数据行数）
 */
private class TableRowBlock(
    private val firstItemIndex: Int,
    private val firstOrdinal: Int,
    private val itemCount: Int,
) {
    /** 该 item 序号对应的行序号；不属于本块时为 null。 */
    fun ordinalAt(itemIndex: Int): Int? =
        if (itemIndex in firstItemIndex until firstItemIndex + itemCount) {
            firstOrdinal + (itemIndex - firstItemIndex)
        } else {
            null
        }
}

/**
 * [TableLayoutScope] 的收集器：把 DSL 调用记成列与行级条目，再交 [build] 校验。
 */
private class TableDescriptionBuilder<T> : TableLayoutScope<T> {

    private val columns = mutableListOf<TableColumnSpec<T>>()
    private val entries = mutableListOf<TableBodyEntry<T>>()

    override fun column(
        width: TableColumnWidth,
        alignment: Alignment,
        header: (@Composable TableCellScope.() -> Unit)?,
        cell: @Composable TableCellScope.(index: Int, item: T) -> Unit,
    ) {
        columns += TableColumnSpec(width, alignment, header, cell)
    }

    override fun spanItem(key: Any?, content: @Composable () -> Unit) {
        entries += TableSpanEntry(key, content)
    }

    override fun rows(items: List<T>, key: ((item: T) -> Any)?) {
        entries += TableRowsEntry(items, key, columns.toList())
    }

    /**
     * 不改读**元素内容**（`entry.items` 的逐个读取会订阅列表变化，让整表在每次重排时重组一次），
     * 只读各块的长度。
     *
     * @param requireItemKey 是否强制 `rows(...)` 给出行 key（行拖拽依赖它）
     */
    fun build(requireItemKey: Boolean): TableDescription<T> {
        val rowsEntries = entries.filterIsInstance<TableRowsEntry<T>>()
        require(rowsEntries.all { it.columns.isNotEmpty() }) {
            "TableLayout: rows(...) 之前必须先声明至少一列 —— column(...) 要写在 rows(...) 前面"
        }
        if (requireItemKey) {
            require(rowsEntries.all { it.itemKey != null }) {
                "TableLayout: 开启行拖拽（onRowMove）时 rows(..., key = { }) 必填 —— 拖拽库按 key 追踪行身份"
            }
        }

        // 惰性 item 序号：表头（若声明）占 0，之后按登记顺序铺开（spanItem 占 1 个 item）
        var itemIndex = if (columns.any { it.header != null }) 1 else 0
        var ordinal = 0
        val rowBlocks = ArrayList<TableRowBlock>(rowsEntries.size)
        entries.forEach { entry ->
            when (entry) {
                is TableSpanEntry -> itemIndex++
                is TableRowsEntry -> {
                    val count = entry.items.size
                    rowBlocks += TableRowBlock(itemIndex, ordinal, count)
                    itemIndex += count
                    ordinal += count
                }
            }
        }
        return TableDescription(columns.toList(), entries.toList(), rowBlocks)
    }
}

/**
 * 一列的宽度修饰（须在 `Row` 作用域内调用）。
 */
private fun RowScope.columnWidthModifier(width: TableColumnWidth): Modifier = when (width) {
    is TableColumnWidth.Fixed    -> Modifier.width(width.width)
    is TableColumnWidth.Fraction -> Modifier.weight(width.weight)
}

/**
 * 表头行：与 [TableRowContent] 同一套列宽修饰，因此两者天然对齐。
 */
@Composable
private fun <T> TableHeaderRow(
    columns: List<TableColumnSpec<T>>,
    columnGap: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier.fillMaxWidth().then(modifier),
        horizontalArrangement = Arrangement.spacedBy(columnGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEach { column ->
            Box(
                modifier = columnWidthModifier(column.width),
                contentAlignment = column.alignment,
            ) {
                val header = column.header
                if (header != null) {
                    val scope = TableCellScopeImpl(
                        box = this,
                        dragHandleFactory = null,
                        isRowDragging = false,
                    )
                    header.invoke(scope)
                }
            }
        }
    }
}

/**
 * 一行数据：每格一个 `Box`，尺寸由列宽修饰决定、内容按列对齐；单元格作用域继承该 `Box` 的
 * [BoxScope]，因此内容可用 `Modifier.align(...)` 覆盖对齐。
 */
@Composable
private fun <T> TableRowContent(
    columns: List<TableColumnSpec<T>>,
    index: Int,
    item: T,
    columnGap: Dp,
    modifier: Modifier = Modifier,
    isRowDragging: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth().then(modifier),
        horizontalArrangement = Arrangement.spacedBy(columnGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEach { column ->
            Box(
                modifier = columnWidthModifier(column.width),
                contentAlignment = column.alignment,
            ) {
                val scope = TableCellScopeImpl(
                    box = this,
                    dragHandleFactory = LocalTableDragHandle.current,
                    isRowDragging = isRowDragging,
                )
                column.cell.invoke(scope, index, item)
            }
        }
    }
}

/**
 * eager 表体：按登记顺序铺开 `spanItem` 与各批数据行。
 *
 * 数据行用 `key(...)` 绑定行 key，重排/增删时行内组合状态跟数据走。
 */
@Composable
private fun <T> TableBodyContent(
    description: TableDescription<T>,
    columnGap: Dp,
    rowModifier: @Composable (index: Int) -> Modifier,
) {
    description.entries.forEach { entry ->
        when (entry) {
            is TableSpanEntry -> entry.content()
            is TableRowsEntry -> {
                val itemKey = entry.itemKey
                entry.items.forEachIndexed { index, item ->
                    val row: @Composable () -> Unit = {
                        TableRowContent(
                            columns = entry.columns,
                            index = index,
                            item = item,
                            columnGap = columnGap,
                            modifier = rowModifier(index),
                        )
                    }
                    val key = itemKey?.invoke(item)
                    if (key != null) key(key) { row() } else row()
                }
            }
        }
    }
}

/**
 * `ReorderableItem` 包装：把拖拽能力下发给单元格作用域。
 *
 * `reorderable` 库的 `Modifier.draggableHandle()` 只在 `ReorderableItem` 的内容作用域内可取，
 * 而单元格内容更靠内层，故把该作用域包成一个 [TableDragHandleFactory] 经 [LocalTableDragHandle]
 * 下发；对外只暴露 `Modifier.dragHandle()`，公开 API 不出现库的类型。
 *
 * @param enabled false 时该行不进入拖拽库的可重排集合（表头、`spanItem` 用）
 */
@Composable
private fun LazyItemScope.ReorderableTableItem(
    state: ReorderableLazyListState,
    itemKey: Any,
    enabled: Boolean,
    content: @Composable (isDragging: Boolean) -> Unit,
) {
    ReorderableItem(state = state, key = itemKey, enabled = enabled) { isDragging ->
        val itemScope = this
        val factory = remember(itemScope) {
            TableDragHandleFactory { modifier, dragEnabled ->
                with(itemScope) { modifier.draggableHandle(enabled = dragEnabled) }
            }
        }
        CompositionLocalProvider(LocalTableDragHandle provides factory) {
            content(isDragging)
        }
    }
}

/** 拖拽手柄工厂：把库的作用域包起来，供 [TableCellScope.dragHandle] 调用。 */
private fun interface TableDragHandleFactory {
    fun create(modifier: Modifier, enabled: Boolean): Modifier
}

/** 当前行可用的拖拽手柄工厂；未接入拖拽的场合为 null。 */
private val LocalTableDragHandle = staticCompositionLocalOf<TableDragHandleFactory?> { null }

/** [TableCellScope] 实现：对齐能力直接委托给所在 `Box`，拖拽能力来自作用域下发的工厂。 */
private class TableCellScopeImpl(
    private val box: BoxScope,
    private val dragHandleFactory: TableDragHandleFactory?,
    override val isRowDragging: Boolean,
) : TableCellScope, BoxScope by box {

    override fun Modifier.dragHandle(enabled: Boolean): Modifier =
        dragHandleFactory?.create(this, enabled) ?: this
}
