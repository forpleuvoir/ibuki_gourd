package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.forpleuvoir.ibukigourd.config.translateComment
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icon
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IconButton
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Icons
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.ui.sokitsu.tooltip.tooltip
import moe.forpleuvoir.nebula.common.api.Resettable
import moe.forpleuvoir.nebula.config.ConfigNode

/**
 * 配置行的布局参数与层级——用 CompositionLocal 提供，允许在子树内整体覆盖。
 *
 * 与 sokitsu 组件的 `*Theme` meta 不同：这些是**页面排版量**而非主题量，所以不做 meta 段，
 * 只在组件内用 CompositionLocal 暴露（外层想整体调行距 / 缩进时不必逐个传参）。
 */
object ConfigRowWrapper {

    /** 行内边距。 */
    val LocalPadding = staticCompositionLocalOf { ConfigRowDefaults.Padding }

    /** 名字列与右侧控件列的间距。 */
    val LocalSpacing = staticCompositionLocalOf { ConfigRowDefaults.Spacing }

    /** 当前行层级：0 为顶层，>0 为分组嵌套（决定缩进与是否画分割线）。 */
    val LocalLevel = compositionLocalOf { 0 }

    /** 行首图标槽位（分组 / 类型图标）。 */
    val LocalIcon = compositionLocalOf<(@Composable () -> Unit)?> { null }

    /**
     * 面包屑：显示在配置名之前的"所属分组"前缀。
     *
     * 默认 null（不显示）；搜索结果这类**跨分组的平铺列表**里由调用方提供，
     * 用来消歧同名项（`test_int` 这类名字在多个分组下都会出现）。
     */
    val LocalBreadcrumb = compositionLocalOf<String?> { null }

    /** 当前生效的内边距。 */
    val padding: ConfigRowPadding
        @Composable @ReadOnlyComposable get() = LocalPadding.current

    /** 当前生效的列间距。 */
    val spacing: Dp
        @Composable @ReadOnlyComposable get() = LocalSpacing.current
}

/**
 * 在当前行内边距之上再增加一段左（start）缩进，用于嵌套层级。
 */
@Composable
fun indentedConfigRowPadding(indent: Dp): ConfigRowPadding = ConfigRowWrapper.padding.indented(indent)

/**
 * 配置行骨架：名称列（标题 + 注释 + 截断时的全文气泡）+ 控件槽 + 重置按钮。
 *
 * 像素风下**不画圆角卡片**：常态行完全透明，悬停时铺一层 [ConfigRowTokens.Container] 底色；
 * 分组的边界由 [ConfigGroupWrapper] 的缩进与分割线承担，行自己不做容器。
 *
 * @param config 该行对应的配置节点（取 [ConfigNode.translateText] / [ConfigNode.translateComment]）
 * @param modifier 作用于整行
 * @param horizontalArrangement 名称列与控件列之间的排布
 * @param verticalAlignment 行内垂直对齐
 * @param icon 行首图标槽位，缺省取 [ConfigRowWrapper.LocalIcon]
 * @param resettable 是否显示重置按钮
 * @param onClick 整行点击回调（分组行用来折叠 / 展开）；为 null 时整行不可点
 * @param onReset 重置后的回调
 * @param content 右侧控件槽
 */
@Composable
fun ConfigRowWrapper(
    config: ConfigNode,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    icon: (@Composable () -> Unit)? = ConfigRowWrapper.LocalIcon.current,
    resettable: Boolean = true,
    onClick: (() -> Unit)? = null,
    onReset: () -> Unit = {},
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val container = Color.Unspecified.resolve(ConfigRowTokens.Container)
    val background by animateColorAsState(
        targetValue = if (hovered) container.copy(alpha = ConfigRowTokens.HoverAlpha) else Color.Transparent,
        animationSpec = tween(ConfigRowDefaults.HoverAnimation.inWholeMilliseconds.toInt()),
        label = "configRowBackground",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .hoverable(interactionSource)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(
                start = ConfigRowWrapper.padding.start,
                top = ConfigRowWrapper.padding.top,
                end = ConfigRowWrapper.padding.end,
                bottom = ConfigRowWrapper.padding.bottom,
            ),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                it()
                Spacer(Modifier.width(ConfigRowWrapper.spacing))
            }
            ConfigName(config)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
            if (resettable) ResetButton(config, onReset = { onReset() })
        }
    }
}

/**
 * 配置名：标题一行 + 注释一行（超出省略）。
 *
 * 注释被截断时，悬停延迟 [ConfigRowDefaults.TooltipDelay] 后弹出全文气泡；未截断不弹，
 * 避免每行都冒出气泡。
 *
 * @param config 配置节点
 * @param modifier 作用于名称列
 */
@Composable
fun ConfigName(config: ConfigNode, modifier: Modifier = Modifier) {
    var truncated by remember(config) { mutableStateOf(false) }
    val interactionSource = remember(config) { MutableInteractionSource() }

    val nameModifier = modifier.then(
        if (truncated) {
            Modifier.tooltip(
                interactionSource = interactionSource,
                delay = ConfigRowDefaults.TooltipDelay,
            ) {
                Text(InlineStyleText(config.translateComment.plainText))
            }
        } else {
            Modifier
        }
    )

    val breadcrumb = ConfigRowWrapper.LocalBreadcrumb.current
    val title = InlineStyleText(config.translateText.plainText)
        .let { text -> breadcrumb?.takeIf { it.isNotEmpty() }?.let { Literal("$it › ").append(text) } ?: text }

    Column(
        modifier = nameModifier.hoverable(interactionSource),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            component = title,
            color = Color.Unspecified.resolve(ConfigRowTokens.Title),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            component = InlineStyleText(config.translateComment.plainText),
            color = Color.Unspecified.resolve(ConfigRowTokens.Comment),
            maxLines = ConfigRowDefaults.CommentMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { truncated = it.hasVisualOverflow },
        )
    }
}

/**
 * 重置按钮：把节点恢复默认值，点击时图标转一圈；已处于默认值（不可重置）时禁用。
 *
 * @param resettable 目标节点
 * @param onReset 重置后的回调
 */
@Composable
fun ResetButton(resettable: Resettable, onReset: () -> Unit = {}) {
    val isDefault by resettable.asDefaultState()
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val interactionSource = remember { MutableInteractionSource() }

    IconButton(
        onClick = {
            resettable.resetDefault()
            onReset()
            scope.launch {
                rotation.animateTo(
                    targetValue = rotation.value - 360f,
                    animationSpec = tween(ConfigRowDefaults.ResetAnimation.inWholeMilliseconds.toInt()),
                )
            }
        },
        modifier = Modifier
            .size(ConfigRowDefaults.ResetButtonSize)
            .hoverable(interactionSource)
            .tooltip(interactionSource = interactionSource) {
                Text(IGLang.Misc.reset)
            },
        enabled = !isDefault,
    ) {
        Icon(
            icon = Icons.Reset,
            scale = ConfigRowDefaults.IconScale,
            tint = Color.Unspecified.resolve(ConfigRowTokens.Icon),
            modifier = Modifier.rotate(rotation.value),
        )
    }
}
