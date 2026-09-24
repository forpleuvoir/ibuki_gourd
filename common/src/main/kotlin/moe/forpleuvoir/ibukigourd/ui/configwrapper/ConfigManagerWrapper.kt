package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.config.matchWithTranslate
import moe.forpleuvoir.ibukigourd.config.translateComment
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.HorizontalDivider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Tab
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TabRow
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TextField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.UiStateIdentifier
import moe.forpleuvoir.ibukigourd.ui.sokitsu.UiStateSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalDivider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalFlatScroller
import moe.forpleuvoir.ibukigourd.ui.sokitsu.rememberScrollerAdapter
import moe.forpleuvoir.ibukigourd.ui.sokitsu.toSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip.tooltip
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigManager
import moe.forpleuvoir.nebula.config.ConfigNode

/**
 * 配置管理器页面：**单层导航** —— 左列是分组导航（可滚动）+ 底部常驻的搜索入口，
 * 右侧一页内容。
 *
 * 分组项用 [FlatButton]（常态无底色）：文字左对齐，选中项把 `focused` 素材当常态底色渲染，
 * 素材取 squared 变体（见 [ConfigManagerDefaults.NavItemSprite]）。
 *
 * 搜索**不占顶部**：入口固定在左列底部，点开把右侧内容区换成搜索页（返回 + 输入框 + 结果），
 * 两个页面之间做横向滑动切换；点任意分组项即退出搜索回到该分组。搜索结果跨分组平铺，
 * 命中判定走 [moe.forpleuvoir.ibukigourd.config.matchWithTranslate]
 * （覆盖名称 / 翻译键 / 标题 / 注释），每项带"所属分组"面包屑消歧。
 *
 * 输入状态由本页持有（[TextFieldState]），所以关掉搜索页再打开仍保留上次的关键词。
 *
 * @param manager 目标配置管理器
 * @param modifier 作用于整页
 * @param groupListWidth 左侧分组列宽度
 */
@Composable
fun ConfigManagerWrapper(
    manager: ConfigManager,
    modifier: Modifier = Modifier,
    groupListWidth: Dp = ConfigManagerDefaults.GroupListWidth,
) {
    val pages = remember(manager) { configPages(manager) }
    var selected by remember(manager) { mutableIntStateOf(0) }
    var searching by remember(manager) { mutableStateOf(false) }
    val searchState = rememberTextFieldState()

    val query = searchState.text.toString()
    val searchResult = remember(manager, query) { manager.search(query) }

    Row(modifier.fillMaxSize()) {
        ConfigGroupList(
            pages = pages,
            selected = selected,
            searching = searching,
            onSelect = {
                selected = it
                searching = false
            },
            onSearch = { searching = true },
            modifier = Modifier.width(groupListWidth).fillMaxHeight(),
        )
        VerticalDivider(Modifier.fillMaxHeight(), thickness = ConfigRowDefaults.DividerThickness)
        AnimatedContent(
            targetState = searching,
            modifier = Modifier.weight(1f).fillMaxHeight().padding(ConfigManagerDefaults.ContentPadding),
            transitionSpec = {
                // 进搜索页：新页自右进、旧页向左出；返回时反向
                val direction = if (targetState) 1 else -1
                val duration = ConfigManagerDefaults.ContentTransitionMillis
                (slideInHorizontally(tween(duration)) { width -> direction * width } + fadeIn(tween(duration))) togetherWith
                        (slideOutHorizontally(tween(duration)) { width -> -direction * width } + fadeOut(tween(duration)))
            },
            label = "configManagerContent",
        ) { isSearching ->
            if (isSearching) {
                ConfigSearchPanel(
                    state = searchState,
                    result = searchResult,
                    onClose = { searching = false },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                ConfigPageContent(
                    page = pages.getOrNull(selected),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/** 配置页：一个分组（或管理器的直属条目）对应一页。 */
private data class ConfigPage(
    val title: MutableText,
    val comment: MutableText,
    val nodes: List<ConfigNode>,
)

/**
 * 页签：一页里的一段内容 —— 直属配置项合起来算一签，其后每个直属分组各一签。
 *
 * @param title 页签文字（直属项那一签用页自己的标题）
 * @param comment 分组注释，作为页签的悬停提示
 * @param nodes 该签呈现的节点
 */
private data class ConfigPageTab(
    val title: MutableText,
    val comment: MutableText,
    val nodes: List<ConfigNode>,
)

/** 分页：直属配置项合成一页（若有），其后每个直属分组各一页。 */
private fun configPages(manager: ConfigManager): List<ConfigPage> {
    val pages = mutableListOf<ConfigPage>()
    val items = manager.children.filterIsInstance<Config<*>>()
    if (items.isNotEmpty()) {
        pages += ConfigPage(manager.translateText, manager.translateComment, items)
    }
    manager.children.filterIsInstance<ConfigGroup>().forEach { group ->
        pages += ConfigPage(group.translateText, group.translateComment, group.children.toList())
    }
    return pages
}

/** 把一页拆成页签：直属配置项（若有）一签，其后每个直属分组各一签。 */
private fun ConfigPage.tabs(): List<ConfigPageTab> {
    val tabs = mutableListOf<ConfigPageTab>()
    val items = nodes.filterIsInstance<Config<*>>()
    if (items.isNotEmpty()) tabs += ConfigPageTab(title, comment, items)
    nodes.filterIsInstance<ConfigGroup>().forEach { group ->
        tabs += ConfigPageTab(group.translateText, group.translateComment, group.children.toList())
    }
    return tabs
}

/**
 * 配置页内容：**上层分组摊成页签**，一次只呈现当前页签的一段（子分组不再各自占一整段折叠区）。
 *
 * 只有一签时不画页签行，直接铺内容；切换页签做横向滑动 + 淡入淡出，方向随下标增减翻转。
 * 更深的层级仍是 [ConfigGroupWrapper] 的折叠分组 —— 页签只吃"这一页的直属分组"。
 *
 * @param page 当前选中的页；null 时内容区为空
 * @param modifier 作用于整块
 */
@Composable
private fun ConfigPageContent(page: ConfigPage?, modifier: Modifier = Modifier) {
    if (page == null) {
        ConfigNodesScroller(nodes = emptyList(), modifier = modifier)
        return
    }
    val tabs = remember(page) { page.tabs() }
    var selectedTab by remember(page) { mutableIntStateOf(0) }
    val current = selectedTab.coerceIn(0, (tabs.size - 1).coerceAtLeast(0))

    Column(modifier) {
        if (tabs.size > 1) {
            TabRow(
                selectedTabIndex = current,
                containerColor = Color.Transparent,
                divider = { HorizontalDivider(thickness = ConfigRowDefaults.DividerThickness) },
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == current,
                        onClick = { selectedTab = index },
                        modifier = if (tab.comment.plainText.isNotEmpty()) {
                            Modifier.tooltip { Text(component = tab.comment) }
                        } else {
                            Modifier
                        },
                        text = { Text(component = tab.title) },
                    )
                }
            }
        }
        AnimatedContent(
            targetState = current,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            transitionSpec = {
                // 往右的页签自右进、往左的向左进；一次滑一格
                val direction = if (targetState > initialState) 1 else -1
                val duration = ConfigManagerDefaults.TabSwitchMillis
                (slideInHorizontally(tween(duration)) { width -> direction * width } + fadeIn(tween(duration))) togetherWith
                        (slideOutHorizontally(tween(duration)) { width -> -direction * width } + fadeOut(tween(duration)))
            },
            label = "configPageTab",
        ) { index ->
            ConfigNodesScroller(
                nodes = tabs.getOrNull(index)?.nodes.orEmpty(),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * 搜索结果：呈现用的顶层节点 + 过滤集合（命中项及其全部祖先分组）。
 *
 * @param nodes 只取结果集里**最上层**的节点，交给 [ConfigsWrapper] 渲染
 * @param filter 喂给 [LocalSearchFilter]：分组行据此只列出命中的子项
 */
private data class ConfigSearchResult(
    val nodes: List<ConfigNode>,
    val filter: Set<ConfigNode>,
)

/**
 * 跨分组搜索：命中 [query]（忽略大小写、按字面量）的叶子配置项，连同它们的**全部祖先分组**。
 *
 * 呈现时只取结果集里最上层的节点，于是"落在同一个分组下的若干命中"合并成**一个分组行**，
 * 组内只列命中的子项（[LocalSearchFilter] → [ConfigGroupWrapper]），而不是把每个命中平铺成一行、
 * 各自再背一串所属分组；命中分散在不同分组时，才按分组并列出来。
 *
 * [query] 全空白返回空结果。
 */
private fun ConfigManager.search(query: String): ConfigSearchResult {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return ConfigSearchResult(emptyList(), emptySet())
    val regex = Regex(Regex.escape(trimmed), RegexOption.IGNORE_CASE)
    val matched = mutableSetOf<ConfigNode>()
    leafNodes().filter { it.matchWithTranslate(regex) }.forEach { node ->
        matched += node
        var parent = node.parent
        while (parent != null && parent !is ConfigManager) {
            matched += parent
            parent = parent.parent
        }
    }
    val top = matched.filter { node ->
        val parent = node.parent
        parent == null || parent !in matched
    }
    return ConfigSearchResult(top, matched)
}

/** 管理器下的全部叶子配置项（分组行本身不进结果）。 */
private fun ConfigManager.leafNodes(): List<ConfigNode> {
    val nodes = mutableListOf<ConfigNode>()
    fun collect(group: ConfigGroup) {
        group.children.forEach { child ->
            if (child is ConfigGroup) collect(child) else nodes += child
        }
    }
    collect(this)
    return nodes
}

/**
 * 分组导航项：文字左对齐、贴满整行的 [FlatButton]。
 *
 * 选中时把 `focused` 素材**当常态底色**（[UiStateSprite.normal] 换成 [UiStateSprite.focused]），
 * 于是选中项常亮而按下仍能显示 `pressed`；`normal` / `disabled` 两个状态本就无素材，
 * 未选中项因此完全透明。
 *
 * @param selected 是否当前选中
 * @param onClick 点击回调
 * @param modifier 作用于按钮；背景精灵在其内边距以内绘制，所以外边距要从这里加
 * @param content 按钮内容
 */
@Composable
private fun ConfigNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val sprites = ConfigManagerDefaults.navItemSprite()
    FlatButton(
        onClick = onClick,
        modifier = modifier,
        sprite = if (selected) sprites.copy(normal = sprites.focused) else sprites,
        minSize = ConfigManagerDefaults.NavItemMinSize,
        contentPadding = ConfigManagerDefaults.NavItemPadding,
        contentAlignment = Alignment.CenterStart,
        content = content,
    )
}

/**
 * 左列：分组导航（滚动）+ 底部常驻搜索入口。
 *
 * 搜索入口与分组项**同款底座、同一高度**，只多一个图标；两者之间垫一条分割线，
 * 于是分组列表滚动时搜索入口始终贴在底部，不会被列表推走。
 *
 * @param pages 全部分组页
 * @param selected 当前选中的分组下标（进入搜索页时不高亮任何分组）
 * @param searching 是否处于搜索页
 * @param onSelect 选中分组回调
 * @param onSearch 打开搜索页回调
 * @param modifier 作用于整列
 */
@Composable
private fun ConfigGroupList(
    pages: List<ConfigPage>,
    selected: Int,
    searching: Boolean,
    onSelect: (Int) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(modifier) {
        Row(Modifier.weight(1f).fillMaxWidth()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(scrollState).padding(ConfigManagerDefaults.GroupListPadding),
                verticalArrangement = Arrangement.spacedBy(ConfigManagerDefaults.GroupSpacing),
            ) {
                pages.forEachIndexed { index, page ->
                    ConfigNavItem(
                        selected = index == selected && !searching,
                        onClick = { onSelect(index) },
                        modifier = Modifier.fillMaxWidth().tooltip { Text(component = page.title) },
                    ) {
                        Text(component = page.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            ConfigScrollbar(scrollState)
        }
        HorizontalDivider(thickness = ConfigRowDefaults.DividerThickness)
        ConfigNavItem(
            selected = false,
            onClick = onSearch,
            modifier = Modifier.fillMaxWidth().padding(ConfigManagerDefaults.GroupListPadding),
        ) {
            Icon(Icons.Search, scale = ConfigManagerDefaults.NavItemIconScale)
            Spacer(Modifier.width(ConfigManagerDefaults.NavItemIconSpacing))
            Text(IGLang.Misc.search)
        }
    }
}

/**
 * 滚动条列：**与内容并列**占一条固定宽度的布局列，而不是浮在内容之上。
 *
 * 浮在 `CenterEnd` 上会盖住行尾的重置按钮 / 分组项右边缘；而**没有可滚动空间时整列不出现**
 * （判定用 [ScrollState.maxValue]，与滚动条自身的 `autoHide` 同源），免得页面右侧平白多出一条空位。
 * 本列只影响宽度、不影响高度，所以"出现 → 内容变窄 → 仍可滚"不会来回抖。
 */
@Composable
private fun RowScope.ConfigScrollbar(scrollState: ScrollState) {
    // 首帧滚动容器还没量过（`maxValue` 仍是 ScrollState 的初值、`viewportSize` 为 0），
    // 先按"没有滚动条"处理，免得开场闪出一格空位
    if (scrollState.viewportSize <= 0 || scrollState.maxValue <= 0) return
    Spacer(Modifier.width(ConfigRowDefaults.ScrollbarSpacing))
    Box(Modifier.width(ConfigRowDefaults.ScrollbarWidth).fillMaxHeight()) {
        VerticalFlatScroller(
            adapter = rememberScrollerAdapter(scrollState),
            modifier = Modifier.fillMaxSize(),
            autoHide = true,
            autoFade = true,
        )
    }
}

/**
 * 搜索页：顶行「返回 + 输入框（可清除）」，隔一段间距后是按**分组**组织的结果。
 *
 * 结果不铺平成一行一条：同一分组下的多个命中合并成一个可折叠的分组行，组内只列命中的子项
 * （[LocalSearchFilter] 驱动，见 [ConfigGroupWrapper]），命中分散时才并列几个分组 ——
 * 这样"是不是同一个容器"一眼可见，也不用每行背一串所属分组。
 *
 * 输入框的 [TextFieldState] 由 [ConfigManagerWrapper] 持有，本页只读不持有，
 * 所以返回再进来关键词还在。
 *
 * 返回用 [Icons.ArrowLeft]（带箭杆的整支箭头）而非 [Icons.Back]：后者是 5×10 的细折角，
 * 与输入框头部同为 16×16 画布时视觉重量差一档，看起来像被挤扁。
 *
 * @param state 搜索输入状态
 * @param result 命中结果（顶层节点为空时内容区显示"无"）
 * @param onClose 返回分组页
 * @param modifier 作用于整列
 */
@Composable
private fun ConfigSearchPanel(
    state: TextFieldState,
    result: ConfigSearchResult,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clear: (@Composable () -> Unit)? = if (state.text.isNotEmpty()) {
        {
            IconButton(
                onClick = { state.edit { replace(0, length, "") } },
                contentPadding = ConfigControlDefaults.IconButtonPadding,
            ) {
                Icon(Icons.Close, scale = configIconScale())
            }
        }
    } else null

    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ConfigManagerDefaults.SearchPanelSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.tooltip { Text(IGLang.Misc.back) },
                contentPadding = ConfigControlDefaults.IconButtonPadding,
            ) {
                Icon(Icons.ArrowLeft, scale = configIconScale())
            }
            TextField(
                state = state,
                modifier = Modifier.weight(1f),
                hint = IGLang.Misc.search,
                leadingIcon = {
                    Icon(Icons.Search, scale = configIconScale())
                },
                trailingIcon = clear,
                lineLimits = TextFieldLineLimits.SingleLine,
            )
        }
        Spacer(Modifier.height(ConfigManagerDefaults.SearchPanelResultSpacing))
        CompositionLocalProvider(LocalSearchFilter provides result.filter) {
            ConfigNodesScroller(
                nodes = result.nodes,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

/**
 * 内容区：一组配置节点 + 并列的滚动条；为空时显示"无"。
 *
 * @param nodes 待呈现的节点
 * @param modifier 作用于内容区
 */
@Composable
private fun ConfigNodesScroller(
    nodes: List<ConfigNode>,
    modifier: Modifier = Modifier,
) {
    if (nodes.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(IGLang.Misc.hasNothing)
        }
        return
    }

    val scrollState = rememberScrollState()
    Row(modifier) {
        Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(scrollState)) {
            ConfigsWrapper(nodes)
        }
        ConfigScrollbar(scrollState)
    }
}

/** 配置页面的排版常量。 */
object ConfigManagerDefaults {

    /** 左侧分组列宽度。 */
    val GroupListWidth: Dp = 240.dp

    /** 分组项间距。 */
    val GroupSpacing: Dp = 4.dp

    /** 分组列内边距；搜索入口复用同一套，两处左右边缘因此对齐。 */
    val GroupListPadding: PaddingValues = PaddingValues(8.dp)

    /** 导航项最小尺寸：高度统一，宽度交给容器（`fillMaxWidth`）。 */
    val NavItemMinSize: DpSize = DpSize(0.dp, 40.dp)

    /** 导航项内容内边距（比组件缺省矮一档，配合 32dp 图标把行高钉在 [NavItemMinSize] 上）。 */
    val NavItemPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 4.dp)

    /** 搜索入口图标倍率：16×16 素材 → 32dp。 */
    const val NavItemIconScale: Int = 2

    /** 搜索入口图标与文案的间距。 */
    val NavItemIconSpacing: Dp = 8.dp

    /** 右侧内容区（分组页 / 搜索页共用）的内边距。 */
    val ContentPadding: PaddingValues = PaddingValues(8.dp)

    /** 搜索页顶行「返回按钮 + 输入框」的间距。 */
    val SearchPanelSpacing: Dp = 8.dp

    /** 搜索页顶行与结果列表之间的纵向间距。 */
    val SearchPanelResultSpacing: Dp = 8.dp

    /** 分组页 ⇄ 搜索页的切换时长。 */
    const val ContentTransitionMillis: Int = 200

    /** 配置页内页签切换的时长。 */
    const val TabSwitchMillis: Int = 150

    /**
     * 导航项的四态纹理：`flat_button` 的 squared 变体（切角 1 素材像素，适合铺满整行）。
     *
     * 素材只画了 `pressed` / `focused` 两张，`normal` / `disabled` **无对应资源** →
     * 图集查询落空 → 该状态不渲染背景（保持透明）；日后补上这两个文件即自动生效。
     */
    val NavItemSprite: UiStateIdentifier = UiStateIdentifier(
        normal = identifier("ui/flat_button/1x/normal"),
        pressed = identifier("ui/flat_button/1x/pressed"),
        focused = identifier("ui/flat_button/1x/focused"),
        disabled = identifier("ui/flat_button/1x/disabled"),
    )

    /** 导航项四态精灵：按 [NavItemSprite] 经 UI 图集解析。 */
    fun navItemSprite(): UiStateSprite = NavItemSprite.toSprite()
}
