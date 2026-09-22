package moe.forpleuvoir.ibukigourd.ui.selector

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalColorScheme
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuTheme

/**
 * 选择器触发件：一块可点击的皮肤容器 + 内容槽位 + 展开图标槽位。
 *
 * **不持有任何状态** —— 展开标志、可用性与点击回调全部由参数注入，因此可脱离选择器独立使用；
 * 选择器内部使用时由选择器传入这组参数，两者的展开状态即同一份。
 *
 * 布局为 [Row]：[content] 与 [expandIcon] 依次排列。皮肤与最小尺寸作用在整体上，
 * 内容内边距只作用于两个槽位；皮肤宽度由调用方约束决定，两个槽位按
 * [androidx.compose.foundation.layout.Arrangement.SpaceBetween] 分列两端。
 *
 * 状态与外观：
 * - 皮肤为**单张**纹理（[sprite]），交互状态不切换素材，差异由染色与描边表达；
 * - 悬停或聚焦时 outline 层染 [SelectorColors.selectedOutlineColor]（与 [moe.forpleuvoir.ibukigourd.ui.sokitsu.Button]
 *   同款描边机制，由 [Surface] 承担）；
 * - 禁用态整套取 [disableColors]。
 *
 * @param content 主内容槽位，通常显示当前选中值
 * @param modifier 修饰，尺寸约束优先于组件默认值
 * @param expandIcon 展开图标槽位，收到当前 [expanded] 与**末端内边距**（[contentPadding] 的
 *   End 值，dp）；默认 [SelectorTriggerDefaults.expandIcon]（一道竖直分割线 +
 *   `Icons.Down` / `Icons.Up` 箭头），传 `{}` 即不显示图标
 * @param enabled 是否可用；false 时不响应点击与悬停
 * @param expanded 当前是否展开，透传给 [expandIcon]
 * @param colors 配色集，默认 [SelectorTriggerDefaults.colors]
 * @param disableColors 禁用态配色集，默认 [SelectorTriggerDefaults.disableColors]；
 *   [enabled] 为 false 时整套取此处
 * @param sprite 皮肤精灵，默认 [SelectorTriggerDefaults.sprite]
 * @param contentPadding 内容内边距，默认 [SelectorTriggerDefaults.padding]
 * @param interactionSource 交互源，不传则内部新建
 * @param onClick 点击回调
 */
@Composable
fun SelectorTrigger(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    expanded: Boolean = false,
    expandIcon: @Composable (expanded: Boolean, contentPaddingEnd: Dp) -> Unit =
        SelectorTriggerDefaults::expandIcon,
    colors: SelectorColors = SelectorTriggerDefaults.colors(),
    disableColors: SelectorColors = SelectorTriggerDefaults.disableColors(),
    sprite: SokitsuSprite = SelectorTriggerDefaults.sprite(),
    contentPadding: PaddingValues = SelectorTriggerDefaults.padding,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val focused by source.collectIsFocusedAsState()

    val palette = if (enabled) colors else disableColors
    // 悬停 / 聚焦：仅 outline 层覆盖为高亮色，其余层保持槽位色
    val outlineColor = if (enabled && (hovered || focused)) palette.selectedOutlineColor else Color.Unspecified
    val contentColor = palette.contentColor

    val hoverIcon = if (enabled) PointerIcon.Hand else PointerIcon.Default

    // 末端内边距：展开图标槽位据此把"分割线 → 箭头"的间距对齐到皮肤右边界
    val padEnd = contentPadding.calculateEndPadding(LocalLayoutDirection.current)

    Surface(
        onClick = onClick,
        modifier = modifier
            .pointerHoverIcon(hoverIcon)
            .semantics { role = Role.Button }
            .defaultMinSize(SelectorTriggerDefaults.minSize.width, SelectorTriggerDefaults.minSize.height),
        enabled = enabled,
        color = palette.color,
        contentColor = contentColor,
        outlineColor = outlineColor,
        sprite = sprite.takeIf { !it.isEmpty },
        textStyle = SokitsuTheme.typography.button,
        contentAlignment = Alignment.CenterStart,
        interactionSource = source,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(Modifier.weight(1f, fill = false)) {
                content()
            }
            val scheme = LocalColorScheme.current
            CompositionLocalProvider(LocalColorScheme provides scheme.copy(outline = outlineColor.takeOrElse { scheme.outline })) {
                expandIcon(expanded, padEnd)
            }
        }
    }
}


/**
 * 选择器触发件配色集：皮肤色板 + 内容色 + 描边高亮色。
 *
 * 禁用态用一个**独立的 [SelectorColors]**（见 [SelectorTrigger] 的 `disableColors`），
 * 而不是在色集里加字段。
 */
@Immutable
data class SelectorColors(
    /** 皮肤精灵的染色色板。 */
    val color: Color,
    /** 内容色。 */
    val contentColor: Color,
    /** 悬停 / 聚焦时 outline 层的高亮色。 */
    val selectedOutlineColor: Color,
)
