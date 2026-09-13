package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.util.contrastContentColor
import moe.forpleuvoir.ibukigourd.util.contrasting

/**
 * 颜色按钮：底色由调用方以**具体颜色**（[color]）给定的 [Button]。
 *
 * 与 [Button] 的唯一区别是配色来源：Button 的底色来自颜色槽位（`ButtonTokens.Container` → 主题 primary），
 * ColorButton 直接吃调用方给的色值，因此
 * - 不参与槽位解析（不会因作用域/主题切换而改变）；
 * - 内容色无法按槽位配对（任意色不在 `ColorScheme` 槽位表里），改为按底色亮度取**纯黑或纯白**
 *   （见 [contrastContentColor]），可用 [contentColor] 覆盖。
 *
 * 其余（渲染、交互、音效、最小尺寸语义）整块委托 [Button]：
 * 背景精灵按下发色染色——`color_button` 素材里 `level=tone` 的层随 [color] 走 Multiply，
 * `level=outline` 的层走描边色，`level=none` 的层直出。
 *
 * 悬停/聚焦时描边覆盖为 [outlineColor]；缺省与 [Button] 同规则，取 [color] 的鲜艳互补色（[contrasting]）。
 *
 * @param color 容器底色（原样使用，不做主题解析）
 * @param contentColor 内容色覆盖；未指定按 [color] 亮度取黑/白
 * @param outlineColor 悬停/聚焦描边覆盖；未指定取 [color] 的互补色
 * @param disabledContentColor 禁用态内容色覆盖；未指定为内容色 × [ColorButtonTokens.DisabledContentOpacity]
 */
@Composable
fun ColorButton(
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = Color.Unspecified,
    outlineColor: Color = Color.Unspecified,
    disabledContentColor: Color = Color.Unspecified,
    sprite: UiStateSprite = ColorButtonDefaults.sprite(),
    contentPadding: PaddingValues = ColorButtonDefaults.contentPadding,
    minSize: DpSize = ColorButtonDefaults.minSize,
    role: Role = Role.Button,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ColorButtonDefaults.colors(color, contentColor, outlineColor, disabledContentColor),
        sprite = sprite,
        contentPadding = contentPadding,
        minSize = minSize,
        role = role,
        interactionSource = interactionSource,
        content = content,
    )
}

object ColorButtonDefaults {

    /** 当前主题的颜色按钮 meta（sprite / 最小尺寸 / 内边距）；内联转发 [SokitsuThemeMeta.colorButton]。 */
    inline val meta get() = SokitsuThemeMeta.colorButton

    /**
     * 由具体底色 [color] 构造按钮配色集。
     *
     * - 容器色 = [color] 原样（本组件的色调不来自槽位）
     * - 内容色 = 未指定 [contentColor] 时取 [color] 的黑白对比色（[contrastContentColor]）
     * - 禁用态内容色 = 未指定 [disabledContentColor] 时取内容色 × [ColorButtonTokens.DisabledContentOpacity]
     *   （只压透明度，不换槽位色）
     * - 悬停/聚焦描边 = 未指定 [outlineColor] 时取 [color] 的鲜艳互补色（[contrasting]），
     *   与 [ButtonDefaults.colors] 同规则 —— 普通态描边取主题 `ColorScheme.outline`，
     *   高亮态若沿用同一色则看不出变化
     */
    @Composable
    fun colors(
        color: Color,
        contentColor: Color = Color.Unspecified,
        outlineColor: Color = Color.Unspecified,
        disabledContentColor: Color = Color.Unspecified,
    ): ButtonColors {
        val resolvedContent = contentColor.takeOrElse { color.contrastContentColor() }
        val resolvedDisabledContent = disabledContentColor.takeOrElse {
            resolvedContent.copy(alpha = resolvedContent.alpha * ColorButtonTokens.DisabledContentOpacity)
        }
        return ButtonColors(
            contentColor = UiStateColor(
                normal = resolvedContent,
                pressed = resolvedContent,
                focused = resolvedContent,
                disabled = resolvedDisabledContent,
            ),
            color = color,
            selectedOutlineColor = outlineColor.takeOrElse { color.contrasting() },
        )
    }

    /** 四态精灵：来自颜色按钮 meta（`ui/color_button/{normal,pressed,focused,disabled}`）。 */
    fun sprite(): UiStateSprite = meta.sprite.toSprite()

    /** 最小尺寸（**单位 dp**，来自主题 meta 的 color_button 段）。 */
    val minSize: DpSize get() = meta.minSize

    /** 内容内边距（水平 = meta.padding，垂直 = meta.padding）。 */
    val contentPadding: PaddingValues get() = meta.padding
}
