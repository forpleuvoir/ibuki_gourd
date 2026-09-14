package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta

/**
 * 图标按钮：[FlatButton] 的薄封装，内容为图标。
 *
 * 尺寸与内边距取自**独立的 `icon_button` meta**（缺省 48×48 dp 最小尺寸 + **四边相等 6dp** 内边距），
 * 需要别的尺寸/留白时传 [contentPadding] / [minSize] 覆盖。渲染、交互、配色全部沿用 [FlatButton]
 * （含"无素材状态不画背景"的策略），四态纹理缺省与扁平按钮走同一批素材。
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
    sprite: UiStateSprite = IconButtonDefaults.sprite(),
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

    /** 当前主题的图标按钮 meta（`ui_meta.icon_button`）。 */
    inline val meta get() = SokitsuThemeMeta.iconButton

    /** 内容内边距（**四边相等**，来自 [IconButtonMeta.padding]）。 */
    val contentPadding: PaddingValues get() = meta.padding

    /** 最小尺寸（来自 [IconButtonMeta.minSize]，缺省 56×56 dp）。 */
    val minSize: DpSize get() = meta.minSize

    /** 四态精灵：来自 [IconButtonMeta.sprite]（缺省与扁平按钮指向同一批素材）。 */
    fun sprite(): UiStateSprite = meta.sprite.toSprite()
}
