package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.graphicsLayer
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite
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
 * **整组是一块**：悬停标题行时，[ConfigRowTokens.Container] 底色铺满标题 + 子项（而不是只亮标题
 * 那一行），子项各自悬停时仍只亮自己那一行。底色由本组件画在整组之后，标题行因此
 * 以 `hoverHighlight = false` 交出自己那层底。
 *
 * 展开 / 收起经 [AnimatedVisibility] 做纵向张开 + 淡入淡出；展开指示沿用选择器同款折角，
 * 见下方注释。搜索期间（[LocalSearchFilter] 非 null）默认展开，且只列出命中的子项。
 *
 * @param config 分组节点
 * @param modifier 作用于整组
 */
@Composable
fun ConfigGroupWrapper(config: ConfigGroup, modifier: Modifier = Modifier) {
    val limit = LocalConfigGroupAutoExpandLimit.current
    val level = ConfigRowWrapper.LocalLevel.current

    val filter = LocalSearchFilter.current
    val children = remember(config, filter) {
        if (filter == null) config.children.toList() else config.children.filter { it in filter }
    }
    // 搜索期间默认展开：命中的子项藏在折叠的分组里等于没搜到；关键词一变就重置回默认
    var expanded by remember(config, filter) {
        mutableStateOf(filter != null || config.children.size < limit)
    }

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val hoverAlpha by animateFloatAsState(
        targetValue = if (hovered) 1f else 0f,
        animationSpec = tween(ConfigRowDefaults.HoverAnimation.inWholeMilliseconds.toInt()),
        label = "configGroupHoverAlpha",
    )
    val container = Color.Unspecified.resolve(ConfigRowTokens.Container)

    Box(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = hoverAlpha }
                .sokitsuSprite(ConfigRowTokens.HoverSprite, color = container),
        )

        CompositionLocalProvider(ConfigRowWrapper.LocalLevel provides level + 1) {
            Column(Modifier.fillMaxWidth()) {
                ConfigRowWrapper(
                    config = config,
                    resettable = false,
                    onClick = { expanded = !expanded },
                    hoverHighlight = false,
                    interactionSource = source,
                ) {
                    // 展开指示与选择器同款（`Icons.Down` 折角）：`up` 素材正是 `down` 上下翻转，
                    // 对这张左右对称的折角来说等价于旋转 180°，所以这里用同一张素材转 ——
                    // 展开态渲染出来就是 `Icons.Up`，又保住了中间过程的过渡动画
                    val rotation by animateFloatAsState(if (expanded) 180f else 0f)
                    Icon(
                        icon = Icons.Down,
                        scale = ConfigRowDefaults.ExpandIconScale,
                        tint = Color.Unspecified.resolve(ConfigRowTokens.Icon),
                        modifier = Modifier.rotate(rotation),
                    )
                }

                val childPadding = indentedConfigRowPadding(ConfigRowDefaults.Indent)
                val durationMillis = ConfigRowDefaults.ExpandAnimation.inWholeMilliseconds.toInt()

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(tween(durationMillis)) + fadeIn(tween(durationMillis)),
                    exit = shrinkVertically(tween(durationMillis)) + fadeOut(tween(durationMillis)),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(ConfigRowDefaults.Spacing),
                    ) {
                        children.forEach { child ->
                            CompositionLocalProvider(ConfigRowWrapper.LocalPadding provides childPadding) {
                                ConfigUiWrapper(child)
                            }
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
 * 顶层（`level == 0`）直接按 [verticalArrangement] 排列；嵌套层级内整体缩进一级
 * （[ConfigRowDefaults.Indent]），与 [ConfigGroupWrapper] 的子项呈现保持一致。
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
    if (level <= 0) {
        configs.forEach { ConfigUiWrapper(it) }
    } else {
        val childPadding = indentedConfigRowPadding(ConfigRowDefaults.Indent)
        configs.forEach { config ->
            CompositionLocalProvider(ConfigRowWrapper.LocalPadding provides childPadding) {
                ConfigUiWrapper(config)
            }
        }
    }
}
