package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize

/**
 * 文本按钮：[FlatButton] 的薄封装，内容为一行文本。
 *
 * 与 [FlatButton] 的差别只有"文本作为显式参数"（少一层 `content` lambda），
 * 渲染、交互、配色全部沿用 [FlatButton]（含"无素材状态不画背景"的策略）。
 *
 * 需要图标+文字等自定义内容时直接用 [FlatButton]。
 *
 * @param text 按钮文字（内容色由 [FlatButton] 下发，无需显式传色）
 */
@Composable
fun TextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: FlatButtonColors = FlatButtonDefaults.colors(),
    sprite: UiStateSprite = FlatButtonDefaults.sprite(),
    contentPadding: PaddingValues = TextButtonDefaults.contentPadding,
    minSize: DpSize = TextButtonDefaults.minSize,
    role: Role = Role.Button,
    interactionSource: MutableInteractionSource? = null,
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
    ) {
        Text(text)
    }
}

object TextButtonDefaults {

    /** 内容内边距：沿用扁平按钮 meta 的 flat_button 段（文本两侧需要留白）。 */
    val contentPadding: PaddingValues get() = FlatButtonDefaults.contentPadding

    /** 最小尺寸：沿用扁平按钮 meta 的 flat_button 段（**单位 dp**）。 */
    val minSize: DpSize get() = FlatButtonDefaults.minSize
}
