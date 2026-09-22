package moe.forpleuvoir.ibukigourd.ui.selector

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.lang.MiscLang
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButtonDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButtonTokens
import moe.forpleuvoir.ibukigourd.ui.sokitsu.UiStateSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.menu.DropdownMenuDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.menu.dropdownMenuAnchor
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuPixelScale
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve

/**
 * 单选选择器：触发件 + 展开体。
 *
 * 空选由泛型承载 —— `T` 可空时选择器即可空选，不额外设"未选中"标志位；
 * 触发器上显示什么由 [content] 决定（`null` 时可显示占位文案）。
 *
 * 展开标志由选择器自持（[expanded]），点击触发件切换；点选某项后自动收起，
 * 不提供"再次点击取消选中"。
 *
 * 展开体的载体由 [expandStyle] 决定（见 [SelectorExpandStyle]）；定义了 [searchFilter] 时
 * 一律走弹窗。
 *
 * @param selected 当前选中值
 * @param onSelect 选中回调
 * @param items 全部选项
 * @param content 触发器内容槽位，收到当前选中值
 * @param modifier 修饰
 * @param enabled 是否可用
 * @param itemEquals 选项相等判定，默认 `==`
 * @param itemContent 选项内容槽位，收到选项与是否选中
 * @param itemLeadingIcon 前置图标槽位工厂：收"是否选中"，返回图标槽位或 null
 * @param itemTrailingIcon 后置图标槽位工厂：收"是否选中"，返回图标槽位或 null
 * @param searchFilter 搜索过滤：收选项与查询词，返回是否保留；非 null 时强制走弹窗
 * @param expandStyle 展开体载体样式，默认 [SelectorExpandStyle.Auto]
 * @param onExpandedChange 展开状态变化回调
 * @param expandStyle 展开体载体样式，默认 [SelectorExpandStyle.Auto]
 */
@Composable
fun <T> Selector(
    selected: T,
    onSelect: (T) -> Unit,
    items: List<T>,
    content: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemEquals: (T, T) -> Boolean = { a, b -> a == b },
    itemContent: @Composable (T, Boolean) -> Unit,
    itemLeadingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)? = null,
    itemTrailingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)? = null,
    searchFilter: ((T, String) -> Boolean)? = null,
    expandStyle: SelectorExpandStyle = SelectorExpandStyle.Auto(),
    onExpandedChange: ((Boolean) -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorBounds by remember { mutableStateOf(Rect.Zero) }

    SelectorTrigger(
        content = { content(selected) },
        modifier = modifier.dropdownMenuAnchor { anchorBounds = it },
        enabled = enabled,
        expanded = expanded,
        onClick = {
            expanded = !expanded
            onExpandedChange?.invoke(expanded)
        },
    )

    SelectorExpanded(
        expanded = expanded,
        onDismissRequest = {
            expanded = false
            onExpandedChange?.invoke(false)
        },
        items = items,
        selected = selected,
        onSelect = onSelect,
        anchorBounds = anchorBounds,
        itemEquals = itemEquals,
        itemContent = itemContent,
        itemLeadingIcon = itemLeadingIcon,
        itemTrailingIcon = itemTrailingIcon,
        searchFilter = searchFilter,
        style = expandStyle,
    )
}

/**
 * 多选选择器：触发件 + 多选展开体。
 *
 * 与单选 [Selector] 只有三处差别，触发器与载体的开合、定位逻辑完全一致：
 * 1. [selected] 是**集合**而非单值，空集即"什么都没选"；
 * 2. 点击某项是**切换**该项的选中态，不关闭展开体；
 * 3. 弹窗载体多出一行确定 / 取消（下拉载体没有）。
 *
 * [onSelectionChange] 在**每次点击条目**时回调一次，语义为"把 [T] 这一项设为选中或取消选中"：
 * - 第一个参数是**点这项之前**的选中集合，第二个参数是本次被点的项；
 * - 调用方据此自行算新集合：`if (item in before) before - item else before + item`；
 * - 取消按钮**不**走本回调，而是交回 [onCancel]（纯通知，不含选中信息），
 *   是否回滚由调用方决定 —— 本组件不持有"展开开始时的集合"，也无从替调用方回滚。
 *
 * 展开期间内部另持一份集合用于渲染（初值取自 [selected]），收起即销毁；
 * 调用方只要在 [onSelectionChange] 里更新 [selected]，两边保持一致。
 *
 * @param selected 当前选中集合，空集即未选
 * @param onSelectionChange 条目被点击：收"点击前的选中集合"与"被点的项"
 * @param items 全部选项
 * @param content 触发器内容槽位，收到当前选中集合
 * @param modifier 修饰
 * @param enabled 是否可用
 * @param itemEquals 选项相等判定，默认 `==`
 * @param itemContent 选项内容槽位，收到选项与是否选中
 * @param itemLeadingIcon 前置图标槽位工厂：收"是否选中"，返回图标槽位或 null
 * @param itemTrailingIcon 后置图标槽位工厂：收"是否选中"，返回图标槽位或 null
 * @param searchFilter 搜索过滤：收选项与查询词，返回是否保留；非 null 时强制走弹窗
 * @param expandStyle 展开体载体样式，默认 [SelectorExpandStyle.Auto]
 * @param onExpandedChange 展开状态变化回调
 * @param onCancel 弹窗载体点取消时回调；下拉载体不会触发
 */
@Composable
fun <T> Selector(
    selected: Set<T>,
    onSelectionChange: (Set<T>, T) -> Unit,
    items: List<T>,
    content: @Composable (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemEquals: (T, T) -> Boolean = { a, b -> a == b },
    itemContent: @Composable (T, Boolean) -> Unit,
    itemLeadingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)? = null,
    itemTrailingIcon: ((Boolean) -> (@Composable (T) -> Unit)?)? = null,
    searchFilter: ((T, String) -> Boolean)? = null,
    expandStyle: SelectorExpandStyle = SelectorExpandStyle.Auto(),
    onExpandedChange: ((Boolean) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorBounds by remember { mutableStateOf(Rect.Zero) }
    // 展开期间用于渲染的选中集合；key = expanded，收起即重建、以最新的 selected 为基准
    val selection = rememberMutableSelection(selected, itemEquals, expanded)

    SelectorTrigger(
        content = { content(selected) },
        modifier = modifier.dropdownMenuAnchor { anchorBounds = it },
        enabled = enabled,
        expanded = expanded,
        onClick = {
            expanded = !expanded
            onExpandedChange?.invoke(expanded)
        },
    )

    SelectorExpanded(
        expanded = expanded,
        onDismissRequest = {
            expanded = false
            onExpandedChange?.invoke(false)
        },
        items = items,
        // 选中集合由上面的 state 承载，展开体不另算（传 null 即告知走多选路径）
        selected = null,
        onSelect = {},
        anchorBounds = anchorBounds,
        itemEquals = itemEquals,
        itemContent = itemContent,
        itemLeadingIcon = itemLeadingIcon,
        itemTrailingIcon = itemTrailingIcon,
        searchFilter = searchFilter,
        style = expandStyle,
        selection = selection,
        // 每次点击都交回"点击前的集合 + 被点的项"
        onItemToggle = { item, before -> onSelectionChange(before, item) },
        onCancel = onCancel,
    )
}

/**
 * 单选选择器的选项条目：单值语义的 [SelectorItem] 重载（选中判定走 `==`）。
 *
 * 多选用 [SelectorItem] 的 `selected: Boolean` 重载。
 */
@Composable
fun <T> SelectorItem(
    item: T,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: T,
    enabled: Boolean = true,
    minHeight: Dp = SelectorItemDefaults.minHeight,
    padding: PaddingValues = SelectorItemDefaults.padding,
    iconSpacing: Dp = SelectorItemDefaults.iconSpacing,
    iconScale: Int = SelectorItemDefaults.iconScale,
    colors: SelectorItemColors = SelectorItemDefaults.colors(),
    sprite: UiStateSprite = SelectorItemDefaults.sprite(),
    contentAlignment: Alignment = SelectorItemDefaults.contentAlignment,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    SelectorItem(
        onClick = onClick,
        modifier = modifier,
        selected = selected == item,
        enabled = enabled,
        minHeight = minHeight,
        padding = padding,
        iconSpacing = iconSpacing,
        iconScale = iconScale,
        colors = colors,
        sprite = sprite,
        contentAlignment = contentAlignment,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        content = content,
    )
}

/**
 * 选择器的选项条目：一行可点击的选项，由 [content] 与两个图标槽位组成。
 *
 * 外形与 [moe.forpleuvoir.ibukigourd.ui.sokitsu.menu.DropdownMenuItem] 同一组规格、同一套条目素材，
 * 额外把**选中态**并入状态渲染：选中时把 `focused` 纹理同时用于 `normal` 位，
 * 使之在未悬停时也带高亮底，并以 [SelectorItemColors.selectedContainerColor] 染色
 * （缺省取辅色），与悬停项的默认染色区分开。
 *
 * 宽度即内容宽度（不填充父级）；高度按 [minHeight] 取**下限**，内容更高时条目随之撑开。
 *
 * [selected] 只影响外观，不影响点击行为：再次点击选中项不做反选。
 *
 * 常态与选中态都**常驻底色**（常态按 [SelectorItemColors.containerColor]，缺省取主题主色），
 * 使悬停 / 按下的 `focused` / `pressed` 状态精灵有底可叠，观感与
 * [moe.forpleuvoir.ibukigourd.ui.sokitsu.menu.DropdownMenuItem] 一致。
 *
 * @param onClick 点击回调
 * @param selected 是否选中；选中项以 `focused` 纹理作常驻背景并染 [SelectorItemColors.selectedContainerColor]
 * @param enabled 是否可用
 * @param minHeight 条目最小高度，默认 [SelectorItemDefaults.minHeight]
 * @param padding 条目内边距，默认 [SelectorItemDefaults.padding]
 * @param iconSpacing 图标槽位与内容之间的间距，默认 [SelectorItemDefaults.iconSpacing]
 * @param iconScale 图标槽位的像素放大倍率，默认 [SelectorItemDefaults.iconScale]
 * @param colors 配色集，默认 [SelectorItemDefaults.colors]
 * @param sprite 条目四态精灵，默认 [SelectorItemDefaults.sprite]
 * @param contentAlignment 内容对齐，默认 [Alignment.TopStart]（靠左、顶对齐）
 * @param leadingIcon 前置图标槽位
 * @param trailingIcon 后置图标槽位
 * @param content 条目内容
 */
@Composable
fun SelectorItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    minHeight: Dp = SelectorItemDefaults.minHeight,
    padding: PaddingValues = SelectorItemDefaults.padding,
    iconSpacing: Dp = SelectorItemDefaults.iconSpacing,
    iconScale: Int = SelectorItemDefaults.iconScale,
    colors: SelectorItemColors = SelectorItemDefaults.colors(),
    sprite: UiStateSprite = SelectorItemDefaults.sprite(),
    contentAlignment: Alignment = SelectorItemDefaults.contentAlignment,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    FlatButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        sprite = SelectorItemDefaults.spriteFor(sprite, selected),
        contentPadding = padding,
        minSize = DpSize(Dp.Hairline, minHeight),
        contentAlignment = contentAlignment,
        colors = FlatButtonDefaults.colors(
            color = if (selected) colors.selectedContainerColor else colors.containerColor,
            contentColor = colors.contentColor,
        ),
    ) {
        leadingIcon?.let { slot ->
            CompositionLocalProvider(LocalSokitsuPixelScale provides iconScale) { slot() }
            Spacer(Modifier.width(iconSpacing))
        }
        content()
        trailingIcon?.let { slot ->
            Spacer(Modifier.width(iconSpacing))
            CompositionLocalProvider(LocalSokitsuPixelScale provides iconScale) { slot() }
        }
    }
}

/**
 * 选择器选项条目配色集：常态与选中态各自的背景色板 + 内容色。
 *
 * 选中态用**染色**而非纹理区分（两者共用同一条目素材）。
 */
@Immutable
data class SelectorItemColors(
    /** 常态条目背景精灵的染色色板。 */
    val containerColor: Color,
    /** 选中态条目背景精灵的染色色板。 */
    val selectedContainerColor: Color,
    /** 内容色。 */
    val contentColor: Color,
)

/**
 * 选择器条目的默认值：外形规格与
 * [moe.forpleuvoir.ibukigourd.ui.sokitsu.menu.DropdownMenuItem] 保持一致。
 */
object SelectorItemDefaults {

    /** 条目最小高度。内容更高时条目随之撑开。 */
    val minHeight: Dp = 20.dp

    /** 条目内容内边距。 */
    val padding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 6.dp)

    /** 图标槽位与内容之间的间距。 */
    val iconSpacing: Dp = 4.dp

    /** 图标槽位的像素放大倍率。 */
    val iconScale: Int = 2

    /** 条目内容对齐：靠左、顶对齐。 */
    val contentAlignment: Alignment = Alignment.TopStart

    /** 条目四态精灵：与下拉菜单条目共用同一组素材。 */
    fun sprite(): UiStateSprite = DropdownMenuDefaults.itemSprite()

    /**
     * 按选中态取条目精灵：选中时把 `focused` 纹理复用到 `normal` 位，
     * 使未悬停的选中项也带高亮底。
     */
    fun spriteFor(sprite: UiStateSprite, selected: Boolean): UiStateSprite =
        if (!selected) sprite else sprite.copy(normal = sprite.focused)

    /**
     * 默认配色集：常态背景取 [FlatButtonTokens.Container]（主题主色），
     * 选中态背景取 [ColorSchemeToken.Secondary]（辅色），内容色取 [ColorSchemeToken.OnSurface]。
     *
     * 常态取主色是为了与 [moe.forpleuvoir.ibukigourd.ui.sokitsu.menu.DropdownMenuItem] 的默认观感一致：
     * `focused` / `pressed` 状态精灵叠在主色底上才有可辨的悬停反馈。
     *
     * 三个参数默认均未指定，语义是"调用方没意见，按 token 结合当前主题解析"。
     */
    @Composable
    fun colors(
        containerColor: Color = Color.Unspecified,
        selectedContainerColor: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
    ): SelectorItemColors = SelectorItemColors(
        containerColor = containerColor.resolve(FlatButtonTokens.Container),
        selectedContainerColor = selectedContainerColor.resolve(ColorSchemeToken.Secondary),
        contentColor = contentColor.resolve(ColorSchemeToken.OnSurface),
    )
}
