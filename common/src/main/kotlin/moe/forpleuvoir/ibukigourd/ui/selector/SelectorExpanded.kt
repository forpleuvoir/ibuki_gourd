package moe.forpleuvoir.ibukigourd.ui.selector

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import moe.forpleuvoir.ibukigourd.lang.MiscLang
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TextField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.menu.DropdownMenu

/**
 * 选择器展开体：把选项列表渲染到由 [style] 决定的载体上。
 *
 * 载体分发规则见 [SelectorExpandStyle]；[searchFilter] 非 null 时一律走弹窗
 * （搜索栏只在弹窗里提供）。
 *
 * 选项的渲染由 [item] 决定 —— 收到选项、是否选中、以及点击回调，
 * 缺省的 [item] 用 [SelectorItem]。
 *
 * @param expanded 是否展开；false 时不组合任何内容
 * @param onDismissRequest 请求关闭（点击外部、Esc、选中之后）
 * @param items 全部选项
 * @param selected 当前选中值
 * @param onSelect 选项被点击
 * @param anchorBounds 触发组件在 root 坐标系中的 bounds；下拉菜单载体用作锚点
 * @param style 载体样式，默认 [SelectorExpandStyle.Auto]
 * @param itemEquals 选项相等判定，默认 `==`
 * @param itemContent 选项内容槽位，收到选项与是否选中
 * @param itemLeadingIcon 前置图标槽位工厂：收"是否选中"，返回图标槽位或 null
 * @param itemTrailingIcon 后置图标槽位工厂：收"是否选中"，返回图标槽位或 null
 * @param searchFilter 搜索过滤：收选项与查询词，返回是否保留；非 null 时强制走弹窗
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
    selected: T,
    onSelect: (T) -> Unit,
    anchorBounds: Rect,
    modifier: Modifier = Modifier,
    style: SelectorExpandStyle = SelectorExpandStyle.Auto(),
    itemEquals: (T, T) -> Boolean = { a, b -> a == b },
    itemContent: @Composable (T, Boolean) -> Unit,
    itemLeadingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)? = null,
    itemTrailingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)? = null,
    searchFilter: ((T, String) -> Boolean)? = null,
    dialogTitle: (@Composable () -> Unit)? = null,
    dialogMinWidth: Dp = SelectorExpandedDefaults.dialogMinWidth,
    dialogMaxWidth: Dp = SelectorExpandedDefaults.dialogMaxWidth,
    dialogListMaxHeight: Dp = SelectorExpandedDefaults.dialogListMaxHeight,
) {
    if (!expanded) return

    val useDialog = SelectorExpandedDefaults.resolveUseDialog(style, searchFilter != null, items.size)

    if (useDialog) {
        SelectorDialogExpanded(
            onDismissRequest = onDismissRequest,
            items = items,
            selected = selected,
            onSelect = onSelect,
            modifier = modifier,
            itemEquals = itemEquals,
            itemContent = itemContent,
            itemLeadingIcon = itemLeadingIcon,
            itemTrailingIcon = itemTrailingIcon,
            searchFilter = searchFilter,
            title = dialogTitle,
            minWidth = dialogMinWidth,
            maxWidth = dialogMaxWidth,
            listMaxHeight = dialogListMaxHeight,
        )
    } else {
        SelectorMenuExpanded(
            onDismissRequest = onDismissRequest,
            items = items,
            selected = selected,
            onSelect = onSelect,
            anchorBounds = anchorBounds,
            modifier = modifier,
            itemEquals = itemEquals,
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
    selected: T,
    onSelect: (T) -> Unit,
    anchorBounds: Rect,
    modifier: Modifier,
    itemEquals: (T, T) -> Boolean,
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
            val isSelected = itemEquals(selected, item)
            val leading = itemLeadingIcon?.invoke(isSelected)
            val trailing = itemTrailingIcon?.invoke(isSelected)
            SelectorItem(
                onClick = {
                    onSelect(item)
                    onDismissRequest()
                },
                selected = isSelected,
                leadingIcon = leading?.let { slot -> { slot(item) } },
                trailingIcon = trailing?.let { slot -> { slot(item) } },
            ) {
                itemContent(item, isSelected)
            }
        }
    }
}

/**
 * 弹窗载体的展开体：可选搜索栏 + 选项列表。
 *
 * [searchFilter] 非 null 时在顶部渲染搜索栏（前置放大镜图标 + [MiscLang.search] 提示文本），
 * 列表按查询词过滤；为 null 时直接展示全部选项。
 */
@Composable
private fun <T> SelectorDialogExpanded(
    onDismissRequest: () -> Unit,
    items: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier,
    itemEquals: (T, T) -> Boolean,
    itemContent: @Composable (T, Boolean) -> Unit,
    itemLeadingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)?,
    itemTrailingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)?,
    searchFilter: ((T, String) -> Boolean)?,
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

                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = listMaxHeight),
                ) {
                    items(filtered.size, key = { index -> index }) { index ->
                        val item = filtered[index]
                        val isSelected = itemEquals(selected, item)
                        val leading = itemLeadingIcon?.invoke(isSelected)
                        val trailing = itemTrailingIcon?.invoke(isSelected)
                        SelectorItem(
                            onClick = {
                                onSelect(item)
                                onDismissRequest()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            selected = isSelected,
                            leadingIcon = leading?.let { slot -> { slot(item) } },
                            trailingIcon = trailing?.let { slot -> { slot(item) } },
                        ) {
                            itemContent(item, isSelected)
                        }
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
    val dialogMinWidth: Dp = 200.dp

    /** 弹窗最大宽度。 */
    val dialogMaxWidth: Dp = 400.dp

    /** 弹窗列表区最大高度；超出后列表滚动。 */
    val dialogListMaxHeight: Dp = 480.dp

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
