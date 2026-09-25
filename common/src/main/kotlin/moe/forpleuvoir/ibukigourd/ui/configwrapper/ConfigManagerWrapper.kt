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
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.layout.widthIn
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
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.SurfaceDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Tab
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TabRow
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TextField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TextFieldTokens
import moe.forpleuvoir.ibukigourd.ui.sokitsu.UiStateIdentifier
import moe.forpleuvoir.ibukigourd.ui.sokitsu.UiStateSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalFlatScroller
import moe.forpleuvoir.ibukigourd.ui.sokitsu.rememberScrollerAdapter
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorSchemeToken
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
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
 * @param groupListMinWidth 左侧分组列的最小宽度
 * @param groupListMaxWidth 左侧分组列的最大宽度（实际宽度取分组名固有宽度，夹在两者之间）
 */
@Composable
fun ConfigManagerWrapper(
    manager: ConfigManager,
    modifier: Modifier = Modifier,
    groupListMinWidth: Dp = ConfigManagerDefaults.GroupListMinWidth,
    groupListMaxWidth: Dp = ConfigManagerDefaults.GroupListMaxWidth,
) {
    val pages = remember(manager) { configPages(manager) }
    var selected by remember(manager) { mutableIntStateOf(0) }
    val searchState = rememberTextFieldState()

    val query = searchState.text.toString()
    val searchResult = remember(manager, query) { manager.search(query) }
    // 两块内嵌面板共用一档底色：分组导航 / 配置本体与搜索框是"同一种凹槽"
    val panelColor = Color.Unspecified.resolve(ConfigManagerDefaults.PanelTone)

    // 整页外围留白统一由 Row 给：两块面板的四边留白才一致，两列之间的横向间距也只剩这一处间距
    Row(
        modifier = modifier.fillMaxSize().padding(ConfigManagerDefaults.ContentPadding),
        horizontalArrangement = Arrangement.spacedBy(ConfigManagerDefaults.ColumnSpacing),
    ) {
        // 左列（分组导航）与右列同款：都坐在一张内嵌面板上
        // 宽度不写死：贴合最宽的分组名，再用 min / max 夹住
        Surface(
            modifier = Modifier
                .widthIn(min = groupListMinWidth, max = groupListMaxWidth)
                .width(IntrinsicSize.Max)
                .fillMaxHeight(),
            color = panelColor,
            sprite = SurfaceDefaults.embeddedPanel,
        ) {
            ConfigGroupList(
                pages = pages,
                selected = selected,
                onSelect = {
                    selected = it
                    // 结果区被搜索占着时，点分组要能看到这一页 —— 顺手把关键词清掉
                    searchState.edit { replace(0, length, "") }
                },
                modifier = Modifier.fillMaxSize().padding(ConfigManagerDefaults.EmbedContentPadding),
            )
        }
        // 左列与右侧内容之间**不画分割线**：两块内嵌面板自带边框，再加一条线会显得脏
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
        ) {
            // 搜索框在**内嵌面板之外**、右列最上面：它是工具栏，不是配置本体的一部分；
            // 非空时面板里换成跨分组结果，清空即回到当前页
            ConfigSearchField(state = searchState, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(ConfigManagerDefaults.SearchResultSpacing))
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                color = panelColor,
                sprite = SurfaceDefaults.embeddedPanel,
            ) {
                Column(Modifier.fillMaxSize().padding(ConfigManagerDefaults.EmbedContentPadding)) {
                    if (query.isBlank()) {
                        ConfigPageContent(
                            page = pages.getOrNull(selected),
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        )
                    } else {
                        CompositionLocalProvider(LocalSearchFilter provides searchResult.filter) {
                            ConfigNodesScroller(
                                nodes = searchResult.nodes,
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 常驻搜索框：单行输入 + 头部放大镜 + 非空时的清除按钮。
 *
 * 输入即过滤（[ConfigManagerWrapper] 直接读 [TextFieldState]），不再是"点入口切搜索页"。
 */
@Composable
private fun ConfigSearchField(
    state: TextFieldState,
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

    TextField(
        state = state,
        modifier = modifier,
        hint = IGLang.Misc.search,
        leadingIcon = {
            Icon(Icons.Search, scale = configIconScale())
        },
        trailingIcon = clear,
        lineLimits = TextFieldLineLimits.SingleLine,
    )
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
            // 页签行与下面的配置行之间留一段：页签行的选中指示器就贴在它的底边，
            // 不留这段的话第一行的悬停底色会直接顶到指示器上
            Spacer(Modifier.height(ConfigManagerDefaults.PageContentTopSpacing))
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
 * 左列：分组导航（可滚动），搜索入口不在这里（见 [ConfigSearchField]，常驻在右侧内容区顶部）。
 *
 * 内边距由调用方给（[ConfigManagerDefaults.EmbedContentPadding]，与右列面板内一致），
 * 本组件只负责"列表 + 并列滚动条"。
 *
 * @param pages 全部分组页
 * @param selected 当前选中的分组下标
 * @param onSelect 选中分组回调
 * @param modifier 作用于整列
 */
@Composable
private fun ConfigGroupList(
    pages: List<ConfigPage>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Row(modifier) {
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(ConfigManagerDefaults.GroupSpacing),
        ) {
            pages.forEachIndexed { index, page ->
                ConfigNavItem(
                    selected = index == selected,
                    onClick = { onSelect(index) },
                    modifier = Modifier.fillMaxWidth().tooltip { Text(component = page.title) },
                ) {
                    Text(component = page.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        ConfigScrollbar(scrollState)
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

    /** 左侧分组列的最小宽度。 */
    val GroupListMinWidth: Dp = 200.dp

    /**
     * 左侧分组列的最大宽度。
     *
     * 列宽贴合最宽的分组名，上下由 [GroupListMinWidth] / 本值夹住，
     * 以免分组名长短悬殊时列宽跟着跳动、或过长分组名把内容区挤没。
     */
    val GroupListMaxWidth: Dp = 320.dp

    /** 分组项间距。 */
    val GroupSpacing: Dp = 4.dp

    /** 导航项最小尺寸：高度统一，宽度交给容器（`fillMaxWidth`）。 */
    val NavItemMinSize: DpSize = DpSize(0.dp, 40.dp)

    /** 导航项内容内边距（比组件缺省矮一档，配合 32dp 图标把行高钉在 [NavItemMinSize] 上）。 */
    val NavItemPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 4.dp)

    /** 整页外围留白：两块内嵌面板与页签条面板外沿之间的四边留白。 */
    val ContentPadding: PaddingValues = PaddingValues(16.dp)

    /**
     * 两块内嵌面板（分组导航 / 配置本体）的染色槽位，取 [TextFieldTokens.Container]：
     * 面板与搜索框同为凹槽，同底色才不会一块深一块浅。
     */
    val PanelTone: ColorSchemeToken = TextFieldTokens.Container

    /**
     * 内嵌面板**内部**的内边距：面板是凹槽，内容要离开自己的描边。
     *
     * 凹槽描边本身要占掉约一个素材像素（= pixelScale 个屏幕像素），给小了内容看着仍像贴着描边，
     * 因此与外层 [ContentPadding] 同档。
     */
    val EmbedContentPadding: PaddingValues = PaddingValues(16.dp)

    /** 搜索框与下方内容（页签 / 结果）之间的纵向间距。 */
    val SearchResultSpacing: Dp = 8.dp

    /** 左侧分组列与右侧内容之间的横向间距：与 [SearchResultSpacing] 同档，横向留白不比纵向更宽。 */
    val ColumnSpacing: Dp = SearchResultSpacing

    /** 页签行与配置节点之间的纵向间距（页签选中指示器贴在页签行底边，要给下面留出这段）。 */
    val PageContentTopSpacing: Dp = 12.dp

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
