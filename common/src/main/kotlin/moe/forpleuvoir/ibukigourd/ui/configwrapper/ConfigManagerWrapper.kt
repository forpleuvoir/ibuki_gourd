package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.config.matchWithTranslate
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.Search
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.nebula.config.ConfigManager
import moe.forpleuvoir.nebula.config.groups
import moe.forpleuvoir.nebula.config.items
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigManagerWrapper(
    manager: ConfigManager,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier,
) {
    val groups = buildList {
        val items = manager.items
        if (items.isNotEmpty()) {
            add(manager to items)

        }
        manager.groups.forEach { group ->
            add(group to group.children.toList())
        }
    }

    var currentGroup by remember { mutableStateOf(groups.firstOrNull()?.first?.name) }
    var debouncedRegex by remember { mutableStateOf("") }
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(Unit) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { text ->
                debounceJob?.cancel()
                debounceJob = launch {
                    delay(50.milliseconds)
                    debouncedRegex = text
                }
            }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    TextField(
        state = textFieldState,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        lineLimits = TextFieldLineLimits.SingleLine,
        placeholder = { Text(IGLang.Misc.search) },
        leadingIcon = { Icon(Icons.Search, null) },
        shape = SearchBarDefaults.inputFieldShape,
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        )
    )

    PrimaryScrollableTabRow(
        modifier = Modifier,
        selectedTabIndex = selectedTabIndex
    ) {
        groups.fastForEachIndexed { index, (group, _) ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                    currentGroup = group.name
                },
                text = {
                    Text(group.translateText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            )
        }
    }
    AnimatedContent(
        targetState = currentGroup,
        transitionSpec = {
            val currentIdx = groups.indexOfFirst { it.first.name == initialState }
            val targetIdx = groups.indexOfFirst { it.first.name == targetState }
            val direction = if (targetIdx > currentIdx) 1 else -1
            (slideInHorizontally(spring()) { width -> direction * width } + fadeIn(spring())) togetherWith
                    (slideOutHorizontally(spring()) { width -> -direction * width } + fadeOut(spring()))
        },
        label = "ConfigTabContent"
    ) { groupName ->
        val configs = remember(groupName, debouncedRegex) {
            val r = try {
                if (debouncedRegex.isNotEmpty()) debouncedRegex.toRegex() else null
            } catch (_: Exception) {
                null
            }
            groups.firstOrNull { it.first.name == groupName }?.second?.filter { c ->
                r?.let { r -> c.matchWithTranslate(r) } ?: true
            } ?: emptyList()
        }

        Box(Modifier.fillMaxHeight()) {
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