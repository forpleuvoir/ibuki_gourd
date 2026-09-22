package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import moe.forpleuvoir.ibukigourd.config.matchWithTranslate
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FlatButtonDefaults
import moe.forpleuvoir.ibukigourd.ui.sokitsu.HorizontalDivider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.TextField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalDivider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.VerticalFlatScroller
import moe.forpleuvoir.ibukigourd.ui.sokitsu.rememberScrollerAdapter
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip.tooltip
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigManager
import moe.forpleuvoir.nebula.config.ConfigNode
import moe.forpleuvoir.nebula.config.pathToRoot

/**
 * 配置管理器页面：**单层导航**（左侧分组列表 + 右侧内容），顶部一条搜索框。
 *
 * 与旧版的差别：旧版是 Material3 `PermanentNavigationDrawer` + `PrimaryScrollableTabRow` 双层导航
 * （抽屉选组、页签再选子组），新栈没有这两个组件、双层也冗余；这里改成一层：左侧一列分组，
 * 右侧一页内容。像素风下分组项是普通 `FlatButton`，选中态用 `primaryContainer` 底色，
 * 不做圆角卡片。
 *
 * 搜索：输入非空时切换为**跨分组平铺结果**（只列叶子配置项，命中判定走
 * [moe.forpleuvoir.ibukigourd.config.matchWithTranslate]，覆盖名称 / 翻译键 / 标题 / 注释）。
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
    var query by remember(manager) { mutableStateOf("") }

    val matches = remember(manager, query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            null
        } else {
            val regex = Regex(Regex.escape(trimmed), RegexOption.IGNORE_CASE)
            manager.leafNodes().filter { it.matchWithTranslate(regex) }
        }
    }

    Column(modifier.fillMaxSize()) {
        ConfigSearchBar(query = query, onQueryChange = { query = it })
        HorizontalDivider()

        if (matches != null) {
            ConfigNodesScroller(
                nodes = matches,
                withBreadcrumb = true,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        } else {
            Row(Modifier.weight(1f).fillMaxWidth()) {
                ConfigGroupList(
                    pages = pages,
                    selected = selected,
                    onSelect = { selected = it },
                    modifier = Modifier.width(groupListWidth).fillMaxHeight(),
                )
                VerticalDivider(Modifier.fillMaxHeight())
                ConfigNodesScroller(
                    nodes = pages.getOrNull(selected)?.nodes.orEmpty(),
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
    }
}

/** 配置页：一个分组（或管理器的直属条目）对应一页。 */
private data class ConfigPage(val title: MutableText, val nodes: List<ConfigNode>)

/** 分页：直属配置项合成一页（若有），其后每个直属分组各一页。 */
private fun configPages(manager: ConfigManager): List<ConfigPage> {
    val pages = mutableListOf<ConfigPage>()
    val items = manager.children.filterIsInstance<Config<*>>()
    if (items.isNotEmpty()) {
        pages += ConfigPage(manager.translateText, items)
    }
    manager.children.filterIsInstance<ConfigGroup>().forEach { group ->
        pages += ConfigPage(group.translateText, group.children.toList())
    }
    return pages
}

/** 管理器下的全部叶子配置项（搜索用；分组行本身不进结果）。 */
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

/** 顶部搜索框：受控单行输入（`lastSynced` 守卫，外部值变化才回写文本）。 */
@Composable
private fun ConfigSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberTextFieldState(query)
    var lastSynced by remember { mutableStateOf(query) }

    LaunchedEffect(query) {
        if (query != lastSynced) {
            lastSynced = query
            state.setTextAndPlaceCursorAtEnd(query)
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }.collect { text ->
            if (text != lastSynced) {
                lastSynced = text
                onQueryChange(text)
            }
        }
    }

    TextField(
        state = state,
        modifier = modifier.fillMaxWidth().padding(ConfigManagerDefaults.SearchPadding),
        hint = IGLang.Misc.search,
        leadingIcon = {
            Icon(Icons.Search, scale = configIconScale())
        },
        lineLimits = TextFieldLineLimits.SingleLine,
    )
}

/** 左侧分组列表：选中态用 `primaryContainer` 底色区分。 */
@Composable
private fun ConfigGroupList(
    pages: List<ConfigPage>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = LocalColorScheme.current
    val scrollState = rememberScrollState()

    Box(modifier) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(ConfigManagerDefaults.GroupListPadding),
            verticalArrangement = Arrangement.spacedBy(ConfigManagerDefaults.GroupSpacing),
        ) {
            pages.forEachIndexed { index, page ->
                val isSelected = index == selected
                FlatButton(
                    onClick = { onSelect(index) },
                    modifier = Modifier.fillMaxWidth().tooltip { Text(component = page.title) },
                    colors = if (isSelected) {
                        FlatButtonDefaults.colors(scheme.primaryContainer, scheme.onPrimaryContainer)
                    } else {
                        FlatButtonDefaults.colors()
                    },
                ) {
                    Text(component = page.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        VerticalFlatScroller(
            adapter = rememberScrollerAdapter(scrollState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            autoHide = true,
            autoFade = true,
        )
    }
}

/**
 * 内容区：一组配置节点 + 叠加式滚动条；无命中时显示"无"。
 *
 * @param nodes 待呈现的节点
 * @param withBreadcrumb true 时给每一项带上"所属分组"前缀（搜索结果用，消歧同名项）
 * @param modifier 作用于内容区
 */
@Composable
private fun ConfigNodesScroller(
    nodes: List<ConfigNode>,
    modifier: Modifier = Modifier,
    withBreadcrumb: Boolean = false,
) {
    if (nodes.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(IGLang.Misc.hasNothing)
        }
        return
    }

    val scrollState = rememberScrollState()
    Box(modifier) {
        Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
            if (withBreadcrumb) {
                nodes.forEach { node ->
                    CompositionLocalProvider(
                        ConfigRowWrapper.LocalBreadcrumb provides node.breadcrumb(),
                    ) {
                        ConfigUiWrapper(node)
                    }
                }
            } else {
                ConfigsWrapper(nodes)
            }
        }
        VerticalFlatScroller(
            adapter = rememberScrollerAdapter(scrollState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            autoHide = true,
            autoFade = true,
        )
    }
}

/** 配置页面的排版常量。 */
object ConfigManagerDefaults {

    /** 左侧分组列宽度。 */
    val GroupListWidth: Dp = 240.dp

    /** 分组项间距。 */
    val GroupSpacing: Dp = 4.dp

    /** 分组列内边距。 */
    val GroupListPadding: PaddingValues = PaddingValues(8.dp)

    /** 搜索框内边距。 */
    val SearchPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
}

/** 所属分组路径（不含配置管理器自身）；顶层项返回 null。 */
private fun ConfigNode.breadcrumb(): String? {
    val groups = pathToRoot()
        .filter { it !is ConfigManager && it !== this }
        .map { it.translateText.plainText }
    return groups.takeIf { it.isNotEmpty() }?.joinToString(" › ")
}
