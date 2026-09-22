package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize

/**
 * 文本按钮：[FlatButton] 的薄封装，内容为文字。
 *
 * 尺寸与内边距沿用 [FlatButton] 的 meta（`flat_button` 段，缺省 40×40 dp 最小尺寸 +
 * 水平 12 dp / 垂直 8 dp 内边距），渲染、交互、配色全部沿用 [FlatButton]
 * （含"无素材状态不画背景"的策略）。
 *
 * 内容经 [content] 以可组合槽位传入，与 [IconButton] 一致 —— 文字类型不受限
 * （`String` / [androidx.compose.ui.text.AnnotatedString] / [net.minecraft.network.chat.Component]，
 * 后者用于 i18n 文案，见 [Text] 的重载）；文字 + 图标等混排内容也一并支持。
 *
 * @param content 按钮内容；纯文字通常写 `Text(...)`（内容色由 [FlatButton] 下发，无需显式传色）
 */
@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: FlatButtonColors = FlatButtonDefaults.colors(),
    sprite: UiStateSprite = TextButtonDefaults.sprite(),
    contentPadding: PaddingValues = TextButtonDefaults.contentPadding,
    minSize: DpSize = TextButtonDefaults.minSize,
    role: Role = Role.Button,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    FlatButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        sprite = sprite,
        contentPadding = contentPadding,
        minSize = minSize,
        role = role,
        interactionSource = interactionSource,
        content = content,
    )
}

object TextButtonDefaults {

    /** 内容内边距：沿用扁平按钮 meta 的 flat_button 段（文本两侧需要留白）。 */
    val contentPadding: PaddingValues get() = FlatButtonDefaults.contentPadding

    /** 最小尺寸：沿用扁平按钮 meta 的 flat_button 段（**单位 dp**）。 */
    val minSize: DpSize get() = FlatButtonDefaults.minSize

    /** 四态精灵：沿用扁平按钮 meta 的 flat_button 段。 */
    fun sprite(): UiStateSprite = FlatButtonDefaults.sprite()
}
