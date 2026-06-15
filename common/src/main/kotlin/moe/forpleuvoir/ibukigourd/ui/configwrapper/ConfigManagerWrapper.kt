package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import moe.forpleuvoir.ibukigourd.config.matchWithTranslate
import moe.forpleuvoir.ibukigourd.config.translateComment
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.ArrowBack
import moe.forpleuvoir.ibukigourd.ui.icon.default.Delete
import moe.forpleuvoir.ibukigourd.ui.icon.default.KeyboardArrowLeft
import moe.forpleuvoir.ibukigourd.ui.icon.default.Search
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.ibukigourd.ui.preset.TipBox
import moe.forpleuvoir.nebula.config.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupConfigsWrapper(
    parent: ConfigNode,
    configs: List<ConfigNode>,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier,
) {
    val groups = remember(configs) {
        buildList {
            val items = configs.filterIsInstance<Config<*>>()
            if (items.isNotEmpty()) add(parent to items)
            addAll(configs.filterIsInstance<ConfigGroup>().map { group -> group to group.children.toList() })
        }
    }

    var currentGroup by remember { mutableStateOf(groups.firstOrNull()?.first?.name) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val hasTabs = groups.size > 1
    if (hasTabs) {
        PrimaryScrollableTabRow(
            modifier = Modifier,
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            selectedTabIndex = selectedTabIndex
        ) {
            groups.fastForEachIndexed { index, (group, _) ->
                Tab(
                    modifier = Modifier.height(48.dp).padding(bottom = 6.dp, start = 4.dp, end = 4.dp).clip(MaterialTheme.shapes.medium),
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        currentGroup = group.name
                    },
                    text = {
                        val selected = selectedTabIndex == index
                        TipBox({
                            Text(group.translateComment)
                        }) {
                            if (selected) {
                                Text(group.translateText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            } else {
                                val style = LocalTextStyle.current.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                ProvideTextStyle(style) {
                                    Text(group.translateText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
    AnimatedContent(
        targetState = currentGroup,
        transitionSpec = {
            val currentIdx = groups.indexOfFirst { it.first.name == initialState }
            val targetIdx = groups.indexOfFirst { it.first.name == targetState }
            val direction = if (targetIdx > currentIdx) 1 else -1
            (slideInHorizontally(tween(150)) { width -> direction * width } + fadeIn(tween(150))) togetherWith
                    (slideOutHorizontally(tween(150)) { width -> -direction * width } + fadeOut(tween(150)))
        },
        label = "ConfigTabContent"
    ) { groupName ->
        val configs = remember(groupName) {
            groups.firstOrNull { it.first.name == groupName }?.second ?: emptyList()
        }

        Box(Modifier.fillMaxHeight().then(if (hasTabs) Modifier.padding(top = 8.dp) else Modifier)) {
            // scrollState 在 AnimatedContent 内创建，切换 group 后滚动位置不保留
            val scrollState = rememberScrollState()
            Column(Modifier.verticalScroll(scrollState).fillMaxHeight()) {
                ConfigsWrapper(configs)
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(scrollState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
            )
        }
    }

}

@Composable
fun ConfigManagerWrapper(
    manager: ConfigManager,
    modifier: Modifier = Modifier,
    drawerWidth: Dp = 240.dp
) {
    val groups = remember(manager) {
        buildList {
            val items = manager.items
            if (items.isNotEmpty()) add(manager to items)
            addAll(manager.groups.map { group -> group to group.children.toList() })
        }
    }
    var selectedGroup by remember { mutableStateOf(0) }
    var showSearch by remember { mutableStateOf(false) }
    val allNodes = remember(manager) {
        buildList {
            manager.items.forEach { add(it) }
            fun collect(group: ConfigGroup) {
                group.children.forEach { child ->
                    add(child)
                    if (child is ConfigGroup) collect(child)
                }
            }
            manager.groups.forEach { collect(it) }
        }
    }
    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                PermanentDrawerSheet(Modifier.width(drawerWidth).weight(1f)) {
                    groups.fastForEachIndexed { index, (group) ->
                        Spacer(Modifier.height(8.dp))
                        TipBox(
                            { Text(group.translateComment) },
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.End)
                        ) {
                            NavigationDrawerItem(
                                modifier = Modifier
                                    .height(42.dp)
                                    .padding(NavigationDrawerItemDefaults.ItemPadding),
                                shape = MaterialTheme.shapes.large,
                                label = {
                                    Text(group.translateText, overflow = TextOverflow.Ellipsis)
                                },
                                selected = index == selectedGroup && !showSearch,
                                onClick = {
                                    selectedGroup = index
                                    showSearch = false
                                }
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    NavigationDrawerItem(
                        modifier = Modifier
                            .height(42.dp)
                            .padding(NavigationDrawerItemDefaults.ItemPadding),
                        shape = MaterialTheme.shapes.large,
                        icon = {
                            Icon(Icons.Search, null)
                        },
                        label = {
                            Text(IGLang.Misc.search)
                        },
                        selected = false,
                        onClick = { showSearch = true }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    ) {
        AnimatedContent(
            targetState = showSearch,
            transitionSpec = {
                val direction = if (targetState) 1 else -1
                (slideInHorizontally(tween(300)) { width -> direction * width } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(300)) { width -> -direction * width } + fadeOut(tween(300)))
            },
            label = "SearchContent"
        ) { isSearch ->
            if (isSearch) {
                SearchPanel(nodes = allNodes, onBack = { showSearch = false })
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp).padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 3.dp
                ) {
                    AnimatedContent(
                        modifier = Modifier.padding(16.dp),
                        targetState = selectedGroup,
                        transitionSpec = {
                            fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                        },
                        label = "NavigationContent"
                    ) { index ->
                        GroupConfigsWrapper(
                            groups[index].first,
                            groups[index].second,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchPanel(
    nodes: List<ConfigNode>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textFieldState = rememberTextFieldState()
    var searchResults by remember { mutableStateOf(emptyList<ConfigNode>()) }

    LaunchedEffect(textFieldState.text.toString()) {
        val query = textFieldState.text.toString()
        searchResults = if (query.isBlank()) {
            emptyList()
        } else {
            runCatching { Regex(query) }
                .getOrNull()
                ?.let { regex ->
                    val matched = nodes.filter { it.matchWithTranslate(regex) }
                    // 若某节点及其后代都匹配，只保留后代（更精确的匹配）
                    matched.filter { node ->
                        matched.none { other -> other !== node && other.path.startsWith("${node.path}.") }
                    }
                }
                ?: emptyList()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp).padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.ArrowBack, null)
                }
                Column(Modifier.weight(1f)) {
                    Box(Modifier.fillMaxWidth().height(36.dp), contentAlignment = Alignment.CenterStart) {
                        if (textFieldState.text.isEmpty()) {
                            Text(
                                IGLang.Misc.search,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        BasicTextField(
                            state = textFieldState,
                            lineLimits = TextFieldLineLimits.SingleLine,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                if (textFieldState.text.isNotEmpty()) {
                    IconButton(onClick = { textFieldState.edit { replace(0, length, "") } }) {
                        Icon(Icons.Delete, null)
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(8.dp))
            Box(Modifier.weight(1f)) {
                val scrollState = rememberScrollState()
                Column(Modifier.verticalScroll(scrollState).fillMaxHeight()) {
                    ConfigsWrapper(searchResults)
                }
                VerticalScrollbar(
                    rememberScrollbarAdapter(scrollState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                )
            }
        }
    }
}