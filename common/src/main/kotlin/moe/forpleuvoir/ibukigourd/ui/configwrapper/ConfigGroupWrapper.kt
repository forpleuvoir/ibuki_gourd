package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.defaults.KeyboardArrowDown
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigNode

val LocalSearchFilter = staticCompositionLocalOf<Set<ConfigNode>?> { null }

val LocalAutoExpandConfigGroupLimit = staticCompositionLocalOf { 10 }

@Composable
fun ConfigGroupWrapper(
    config: ConfigGroup,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    val limit = LocalAutoExpandConfigGroupLimit.current
    var expanded by remember { mutableStateOf(config.children.size < limit) }
    val level = ConfigRowWrapper.LocalLevel.current
    // 整个 group（header + 子元素）共用一个圆角矩形，内部层级提升，header 和子项都不画独立圆角矩形
    // 容器只负责圆角裁剪，背景透明；hover 背景由内部行自行动画，避免静态底色与 hover 叠加导致颜色跳变
    // 仅顶层 group 启用圆角裁剪，嵌套层级内不裁剪
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (level == 0) it.clip(MaterialTheme.shapes.medium) else it }
    ) {
        CompositionLocalProvider(
            ConfigRowWrapper.LocalLevel provides ConfigRowWrapper.LocalLevel.current + 1
        ) {
            ConfigRowWrapper(
                config,
                modifier,
                horizontalArrangement,
                verticalAlignment,
                resettable = false,
                onClick = { expanded = !expanded }
            ) {
                val rotation by animateFloatAsState(if (expanded) 180f else 0f)
                Box(Modifier.height(ConfigRowWrapper.entrySize.height), contentAlignment = Alignment.Center) {
                    Icon(Icons.KeyboardArrowDown, null, Modifier.rotate(rotation))
                }
            }
            val searchFilter = LocalSearchFilter.current
            val childrenToShow = if (searchFilter != null) config.children.filter { it in searchFilter } else config.children
            AnimatedVisibility(expanded) {
                Column {
                    childrenToShow.forEach { child ->
                        HorizontalDivider()
                        CompositionLocalProvider(
                            ConfigRowWrapper.LocalPadding provides ConfigRowWrapper.padding + PaddingValues(
                                start = 24.dp,
                                end = 8.dp
                            )
                        ) {
                            ConfigUiWrapper(child)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigsWrapper(
    configs: Iterable<ConfigNode>,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) = Column(modifier, verticalArrangement, horizontalAlignment) {
    val level = ConfigRowWrapper.LocalLevel.current
    configs.forEachIndexed { index, it ->
        // 嵌套层级内（level > 0）用横线分隔，顶层用间距分隔
        if (level > 0 && index > 0) HorizontalDivider()
        if (level > 0) {
            CompositionLocalProvider(
                ConfigRowWrapper.LocalPadding provides ConfigRowWrapper.padding + PaddingValues(
                    start = 24.dp,
                    end = 8.dp
                )
            ) {
                ConfigUiWrapper(it)
            }
        } else {
            ConfigUiWrapper(it)
        }
    }
}
