package moe.forpleuvoir.ibukigourd.ui.selector

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import moe.forpleuvoir.ibukigourd.lang.MiscLang
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.ScrollerDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TextButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TextField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalOverlayScroller
import moe.forpleuvoir.ibukigourd.ui.sokitsu.menu.DropdownMenu
import moe.forpleuvoir.ibukigourd.ui.sokitsu.rememberScrollerAdapter
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import kotlin.math.roundToInt

/**
 * 选择器展开体：把选项列表渲染到由 [style] 决定的载体上。
 *
 * 载体分发规则见 [SelectorExpandStyle]；[searchFilter] 非 null 时一律走弹窗
 * （搜索栏只在弹窗里提供）。
 *
 * 选项的选中态与点击行为由 [selection] 决定 —— 传 null 走单值语义（条目按
 * `itemEquals(selected, item)` 判选中、点击即 [onSelect] 并收起）；传 [MutableSelectionState]
 * 则由它接管（多选即走这条路：点击是切换选中态、不收起，并回调 [SelectorExpanded] 的 `onItemToggle`）。
 *
 * @param expanded 是否展开；false 时不组合任何内容
 * @param onDismissRequest 请求关闭（点击外部、Esc、选中之后）
 * @param items 全部选项
 * @param selected 当前选中值（单选用；多选传 null）
 * @param onSelect 选项被点击（单选用，选中后自动收起）
 * @param anchorBounds 触发组件在 root 坐标系中的 bounds；下拉菜单载体用作锚点
 * @param style 载体样式，默认 [SelectorExpandStyle.Auto]
 * @param itemEquals 选项相等判定，默认 `==`
 * @param itemContent 选项内容槽位，收到选项与是否选中
 * @param itemLeadingIcon 前置图标槽位工厂：收"是否选中"，返回图标槽位或 null
 * @param itemTrailingIcon 后置图标槽位工厂：收"是否选中"，返回图标槽位或 null
 * @param searchFilter 搜索过滤：收选项与查询词，返回是否保留；非 null 时强制走弹窗
 * @param selection 多选状态：非 null 时接管选中态与点击行为（走多选路径）
 * @param onItemToggle 多选下每次切换回调：收"被点的项"与**切换前**的选中集合
 * @param onCancel 多选下弹窗点取消时的回调；null 则弹窗不渲染确定 / 取消行
 * @param dialogTitle 弹窗标题
 * @param dialogMinWidth 弹窗最小宽度
 * @param dialogMaxWidth 弹窗最大宽度
 * @param dialogListMaxHeight 弹窗列表区最大高度
 */
@Composable
fun <T> SelectorExpanded(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<T>,
    selected: Any?,
    onSelect: (T) -> Unit,
    anchorBounds: Rect,
    modifier: Modifier = Modifier,
    style: SelectorExpandStyle = SelectorExpandStyle.Auto(),
    itemEquals: (T, T) -> Boolean = { a, b -> a == b },
    itemContent: @Composable (T, Boolean) -> Unit,
    itemLeadingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)? = null,
    itemTrailingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)? = null,
    searchFilter: ((T, String) -> Boolean)? = null,
    selection: MutableSelectionState<T>? = null,
    onItemToggle: ((T, Set<T>) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    dialogTitle: (@Composable () -> Unit)? = null,
    dialogMinWidth: Dp = SelectorExpandedDefaults.dialogMinWidth,
    dialogMaxWidth: Dp = SelectorExpandedDefaults.dialogMaxWidth,
    dialogListMaxHeight: Dp = SelectorExpandedDefaults.dialogListMaxHeight,
) {
    if (!expanded) return

    val useDialog = SelectorExpandedDefaults.resolveUseDialog(style, searchFilter != null, items.size)

    val toggle: (T) -> Unit
    val isSelected: (T) -> Boolean
    if (selection != null) {
        // 多选：切换选中态、不收起；交回"切换前"的集合快照
        toggle = { item ->
            val before = selection.toSet()
            selection.toggle(item)
            onItemToggle?.invoke(item, before)
        }
        isSelected = { item -> selection.contains(item) }
    } else {
        // 单值语义：点击即替换并收起；选中判定走 itemEquals
        toggle = { item ->
            onSelect(item)
            onDismissRequest()
        }
        isSelected = { item ->
            @Suppress("UNCHECKED_CAST")
            itemEquals(selected as T, item)
        }
    }

    if (useDialog) {
        SelectorDialogExpanded(
            onDismissRequest = onDismissRequest,
            items = items,
            onToggle = toggle,
            isSelected = isSelected,
            modifier = modifier,
            itemContent = itemContent,
            itemLeadingIcon = itemLeadingIcon,
            itemTrailingIcon = itemTrailingIcon,
            searchFilter = searchFilter,
            onCancel = if (selection != null) onCancel else null,
            title = dialogTitle,
            minWidth = dialogMinWidth,
            maxWidth = dialogMaxWidth,
            listMaxHeight = dialogListMaxHeight,
        )
    } else {
        SelectorMenuExpanded(
            onDismissRequest = onDismissRequest,
            items = items,
            onToggle = toggle,
            isSelected = isSelected,
            anchorBounds = anchorBounds,
            modifier = modifier,
            itemContent = itemContent,
            itemLeadingIcon = itemLeadingIcon,
            itemTrailingIcon = itemTrailingIcon,
        )
    }
}

/**
 * 下拉菜单载体的展开体：在 [DropdownMenu] 里逐项渲染 [SelectorItem]。
 */
@Composable
private fun <T> SelectorMenuExpanded(
    onDismissRequest: () -> Unit,
    items: List<T>,
    onToggle: (T) -> Unit,
    isSelected: (T) -> Boolean,
    anchorBounds: Rect,
    modifier: Modifier,
    itemContent: @Composable (T, Boolean) -> Unit,
    itemLeadingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)?,
    itemTrailingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)?,
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismissRequest,
        anchorBounds = anchorBounds,
        modifier = modifier,
    ) {
        items.forEach { item ->
            val selected = isSelected(item)
            val leading = itemLeadingIcon?.invoke(selected)
            val trailing = itemTrailingIcon?.invoke(selected)
            SelectorItem(
                onClick = { onToggle(item) },
                selected = selected,
                leadingIcon = leading?.let { slot -> { slot(item) } },
                trailingIcon = trailing?.let { slot -> { slot(item) } },
            ) {
                itemContent(item, selected)
            }
        }
    }
}

/**
 * 弹窗载体的展开体：可选搜索栏 + 选项列表 +（多选时）确定 / 取消行。
 *
 * [searchFilter] 非 null 时在顶部渲染搜索栏（前置放大镜图标 + [MiscLang.search] 提示文本），
 * 列表按查询词过滤；为 null 时直接展示全部选项。
 *
 * [confirmation] 非 null 时在底部渲染取消 / 确定一行 —— 取消只关闭（放弃本次改动），
 * [onCancel] 非 null 时在底部渲染取消 / 确定一行 —— 取消先 [onCancel] 再关闭，
 * 确定只关闭（条目的点击已在过程中回调过）。
 */
@Composable
private fun <T> SelectorDialogExpanded(
    onDismissRequest: () -> Unit,
    items: List<T>,
    onToggle: (T) -> Unit,
    isSelected: (T) -> Boolean,
    modifier: Modifier,
    itemContent: @Composable (T, Boolean) -> Unit,
    itemLeadingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)?,
    itemTrailingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)?,
    searchFilter: ((T, String) -> Boolean)?,
    onCancel: (() -> Unit)?,
    title: (@Composable () -> Unit)?,
    minWidth: Dp,
    maxWidth: Dp,
    listMaxHeight: Dp,
) {
    val searchState = rememberTextFieldState()
    val query = searchState.text.toString()
    val filtered = remember(items, query, searchFilter) {
        if (searchFilter == null) items else items.filter { searchFilter(it, query) }
    }
    val listState = rememberLazyListState()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true),
    ) {
        Surface(modifier) {
            Column(
                Modifier
                    .widthIn(min = minWidth, max = maxWidth)
                    .padding(SelectorExpandedDefaults.dialogPadding),
                verticalArrangement = Arrangement.spacedBy(SelectorExpandedDefaults.dialogSpacing),
            ) {
                title?.let { it() }

                if (searchFilter != null) {
                    SelectorSearchField(searchState)
                }

                // 列表 + 滚动条（overlay 样式，并列占位）：滚动条与列表同行、占据自身厚度，
                // 有滚动条时列表让出其厚度（总宽不超出弹窗宽度钳制），autoHide 空组合时不让位。
                // 不用 Row+IntrinsicSize：lazy 的固有测量是全量 subcompose 且高度不按 listMaxHeight
                // 收敛会错位，用 Layout 先测列表再测滚动条，每个 child 只测一次。
                // autoHide 为空组合语义：判定数据由列表测量期写入，首次出现 / 溢出状态翻转时晚一帧。
                val (scrollerThickness, scrollSpacing) = with(LocalDensity.current) {
                    ScrollerDefaults.overlayTrackMinSize.width.toPx() to
                        (LocalSokitsuPixelScale.current * 2).dp.toPx()
                }
                Layout({
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = listMaxHeight),
                    ) {
                        items(filtered.size, key = { index -> index }) { index ->
                            val item = filtered[index]
                            val selected = isSelected(item)
                            val leading = itemLeadingIcon?.invoke(selected)
                            val trailing = itemTrailingIcon?.invoke(selected)
                            SelectorItem(
                                onClick = { onToggle(item) },
                                modifier = Modifier.fillMaxWidth(),
                                selected = selected,
                                leadingIcon = leading?.let { slot -> { slot(item) } },
                                trailingIcon = trailing?.let { slot -> { slot(item) } },
                            ) {
                                itemContent(item, selected)
                            }
                        }
                    }
                    VerticalOverlayScroller(
                        adapter = rememberScrollerAdapter(listState),
                        modifier = Modifier.fillMaxHeight(),
                        autoHide = true,
                        autoFade = true,
                    )
                }) { measurables, constraints ->
                    // 有滚动条才让位：列表宽度收敛出滚动条厚度 + 间距
                    val reserved = if (measurables.size > 1) {
                        scrollerThickness.roundToInt() + scrollSpacing.roundToInt()
                    } else {
                        0
                    }
                    val list = measurables[0].measure(
                        if (constraints.hasBoundedWidth) {
                            constraints.copy(maxWidth = (constraints.maxWidth - reserved).coerceAtLeast(0))
                        } else {
                            constraints
                        }
                    )
                    val scroller = measurables.getOrNull(1)?.measure(
                        constraints.copy(minWidth = 0, minHeight = 0, maxHeight = list.height)
                    )
                    layout(
                        width = list.width + (scroller?.let { scrollSpacing.roundToInt() + it.width } ?: 0),
                        height = maxOf(list.height, scroller?.height ?: 0),
                    ) {
                        list.placeRelative(0, 0)
                        // 与列表隔开间距，滚动条在最右
                        scroller?.placeRelative(list.width + scrollSpacing.roundToInt(), 0)
                    }
                }

                onCancel?.let { cancel ->
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = SelectorExpandedDefaults.dialogButtonsTopPadding),
                        horizontalArrangement = Arrangement.spacedBy(SelectorExpandedDefaults.dialogButtonSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = {
                                cancel()
                                onDismissRequest()
                            },
                            text = MiscLang.cancel.string,
                        )
                        TextButton(
                            onClick = onDismissRequest,
                            text = MiscLang.confirm.string,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 弹窗载体的搜索栏：前置放大镜图标 + [MiscLang.search] 提示文本的 [TextField]。
 *
 * 高度不覆盖：沿用输入框 meta 的最小高度，避免把搜索栏压得过扁。
 */
@Composable
private fun SelectorSearchField(state: TextFieldState) {
    TextField(
        state = state,
        modifier = Modifier.fillMaxWidth(),
        hint = MiscLang.search,
        leadingIcon = { Icon(Icons.Search) },
    )
}

/**
 * 展开体的默认规格。这些是组件自身的默认值，不随资源包变化。
 */
object SelectorExpandedDefaults {

    /** 弹窗内容内边距。 */
    val dialogPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)

    /** 弹窗内各段之间的间距。 */
    val dialogSpacing: Dp = 8.dp

    /** 弹窗最小宽度。 */
    val dialogMinWidth: Dp = 300.dp

    /** 弹窗最大宽度。 */
    val dialogMaxWidth: Dp = 600.dp

    /** 弹窗列表区最大高度；超出后列表滚动。 */
    val dialogListMaxHeight: Dp = 480.dp

    /** 弹窗按钮行与列表之间的间距。 */
    val dialogButtonsTopPadding: Dp = 8.dp

    /** 弹窗按钮之间的间距。 */
    val dialogButtonSpacing: Dp = 8.dp

    /**
     * 载体分发判定：按 [SelectorExpandStyle] 的规则求是否走弹窗。
     *
     * 定义了搜索过滤器时强制弹窗（优先级最高），其次看 [style]：
     * [SelectorExpandStyle.Dialog] 恒为弹窗，[SelectorExpandStyle.Dropdown] 恒为下拉菜单，
     * [SelectorExpandStyle.Auto] 按 [itemCount] 是否超过 [SelectorExpandStyle.Auto.maxItems] 判定。
     *
     * @param hasSearchFilter 是否定义了搜索过滤器
     * @param itemCount 选项总数（不随搜索词变化，保证展开期间载体稳定）
     */
    fun resolveUseDialog(
        style: SelectorExpandStyle,
        hasSearchFilter: Boolean,
        itemCount: Int,
    ): Boolean = when {
        hasSearchFilter -> true
        style is SelectorExpandStyle.Dialog -> true
        style is SelectorExpandStyle.Dropdown -> false
        style is SelectorExpandStyle.Auto -> itemCount > style.maxItems
        else -> false
    }
}
