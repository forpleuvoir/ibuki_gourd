package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize

/**
 * 图标按钮：[FlatButton] 的薄封装，内容为图标。
 *
 * 尺寸与内边距沿用 `flat_button` meta（缺省 40×40 dp + 12/8 dp 内边距），需要更紧凑的图标按钮时
 * 传 [contentPadding] / [minSize] 覆盖。渲染、交互、配色全部沿用 [FlatButton]（含"无素材状态不画背景"的策略）。
 *
 * 图标经 [content] 传入——本项目图标方案尚未定（矢量 vs `.aseprite` 像素素材），
 * 故这里不约束类型，传什么画什么（[Text] / `SokitsuSprite` / 未来的 `Icon` 均可）；
 * 方案落定后可在此加一个收 `SokitsuSprite` 的便捷重载。
 *
 * @param content 图标内容；通常 `Modifier.size(...)` 由图标本身或调用方决定
 */
@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: FlatButtonColors = FlatButtonDefaults.colors(),
    sprite: UiStateSprite = FlatButtonDefaults.sprite(),
    contentPadding: PaddingValues = IconButtonDefaults.contentPadding,
    minSize: DpSize = IconButtonDefaults.minSize,
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

object IconButtonDefaults {

    /** 内容内边距：沿用扁平按钮 meta 的 flat_button 段（48×48 下图标可占 24×24）。 */
    val contentPadding: PaddingValues get() = FlatButtonDefaults.contentPadding

    /** 最小尺寸：沿用扁平按钮 meta 的 flat_button 段（缺省 48×48 dp）。 */
    val minSize: DpSize get() = FlatButtonDefaults.minSize
}
