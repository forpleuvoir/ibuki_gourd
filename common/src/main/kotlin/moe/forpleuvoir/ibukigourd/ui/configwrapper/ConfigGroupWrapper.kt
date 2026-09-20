package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.HorizontalDivider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigNode

/**
 * 搜索过滤集合：非 null 时只呈现集合内的节点（分组行本身仍展示，子项被过滤）。
 *
 * 由配置页的搜索框写入（见 [ConfigRowWrapper] 所在包），分组包装器只负责消费。
 */
val LocalSearchFilter = staticCompositionLocalOf<Set<ConfigNode>?> { null }

/** 子项数少于该值时分组默认展开。 */
val LocalConfigGroupAutoExpandLimit = staticCompositionLocalOf { ConfigRowDefaults.AutoExpandLimit }

/**
 * 分组：一行可折叠的标题 + 缩进一级的子节点。
 *
 * 像素风下分组**不做卡片容器**：标题行沿用 [ConfigRowWrapper] 的悬停底色，子项之间用
 * [HorizontalDivider] 分隔、并按层级增加左缩进（[ConfigRowDefaults.Indent]）；
 * 层级经 [ConfigRowWrapper.LocalLevel] 传递，嵌套分组自然再进一级。
 *
 * @param config 分组节点
 * @param modifier 作用于整组
 */
@Composable
fun ConfigGroupWrapper(config: ConfigGroup, modifier: Modifier = Modifier) {
    val limit = LocalConfigGroupAutoExpandLimit.current
    var expanded by remember(config) { mutableStateOf(config.children.size < limit) }
    val level = ConfigRowWrapper.LocalLevel.current

    Column(modifier.fillMaxWidth()) {
        CompositionLocalProvider(ConfigRowWrapper.LocalLevel provides level + 1) {
            ConfigRowWrapper(
                config = config,
                resettable = false,
                onClick = { expanded = !expanded },
            ) {
                val rotation by animateFloatAsState(if (expanded) 180f else 0f)
                Icon(
                    icon = Icons.ArrowDown,
                    scale = ConfigRowDefaults.IconScale,
                    tint = Color.Unspecified.resolve(ConfigRowTokens.Icon),
                    modifier = Modifier.rotate(rotation),
                )
            }

            if (expanded) {
                val filter = LocalSearchFilter.current
                val children = remember(config, filter) {
                    if (filter == null) config.children.toList() else config.children.filter { it in filter }
                }
                val childPadding = indentedConfigRowPadding(ConfigRowDefaults.Indent)
                Column {
                    children.forEach { child ->
                        HorizontalDivider()
                        CompositionLocalProvider(ConfigRowWrapper.LocalPadding provides childPadding) {
                            ConfigUiWrapper(child)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 一组配置节点的纵向排布。
 *
 * 顶层（`level == 0`）用间距分隔；嵌套层级内改用 [HorizontalDivider] 分隔并整体缩进，
 * 与 [ConfigGroupWrapper] 的子项呈现保持一致。
 *
 * @param configs 待呈现的节点（保持传入顺序）
 * @param modifier 作用于整列
 */
@Composable
fun ConfigsWrapper(
    configs: Iterable<ConfigNode>,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(ConfigRowDefaults.Spacing),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) = Column(modifier, verticalArrangement, horizontalAlignment) {
    val level = ConfigRowWrapper.LocalLevel.current
    val childPadding = indentedConfigRowPadding(ConfigRowDefaults.Indent)
    configs.forEachIndexed { index, config ->
        if (level > 0) {
            if (index > 0) HorizontalDivider()
            CompositionLocalProvider(ConfigRowWrapper.LocalPadding provides childPadding) {
                ConfigUiWrapper(config)
            }
        } else {
            ConfigUiWrapper(config)
        }
    }
}
