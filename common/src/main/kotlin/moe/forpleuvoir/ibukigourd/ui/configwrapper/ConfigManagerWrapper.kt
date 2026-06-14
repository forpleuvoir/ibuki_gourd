package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import moe.forpleuvoir.ibukigourd.config.translateComment
import moe.forpleuvoir.ibukigourd.config.translateText
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
                                selected = index == selectedGroup,
                                onClick = { selectedGroup = index }
                            )
                        }
                    }
                }
                //TODO
                Text("Search")
            }
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
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
