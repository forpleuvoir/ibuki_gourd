package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.*
import moe.forpleuvoir.ibukigourd.util.contrasting
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundEvents

/**
 * 按钮：交互状态 → 视觉样式的映射 + 尺寸/内边距/语义角色。
 *
 * **容器渲染整块委托给 [Surface]**（与 Material3 中 Button 委托 `Surface` 的分工一致）：
 * 背景精灵、内容色与文本样式的下发、点击交互与音效，全部由 [Surface] 承担；
 * 按钮自身只负责：
 * - 由 [ButtonState.resolve] 推导交互状态并取对应 [ButtonStateStyle]（精灵 + 内容色）；
 * - 悬停/聚焦时把描边层覆盖为 [ButtonColors.selectedOutlineColor]；
 * - 最小尺寸（[ButtonDefaults.minSize]，可用 [minSize] 覆盖，如 [RadioButton] 传自己的 meta）、
 *   内容内边距、语义角色（默认 `Role.Button`，可用 [role] 覆盖）。
 */
@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.colors(),
    sprite: UiStateSprite = ButtonDefaults.sprite(),
    contentPadding: PaddingValues = ButtonDefaults.contentPadding,
    minSize: DpSize = ButtonDefaults.minSize,
    role: Role = Role.Button,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val state = UiState.resolve(enabled, pressed, hovered, focused)

    // 悬停/聚焦：仅 outline 层覆盖为 selectedOutlineColor（描边高亮），其余层保持色板明暗结构
    val tone = when {
        hovered || focused -> colors.tone.copy(outline = colors.selectedOutlineColor)
        else               -> colors.tone
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .semantics { this.role = role }
            .defaultMinSize(minSize.width, minSize.height),
        enabled = enabled,
        tone = tone,
        contentColor = colors.contentColor[state],
        sprite = sprite[state],
        textStyle = SokitsuTheme.typography.button,
        contentAlignment = Alignment.Center,
        pressSound = ButtonDefaults.LocalPressSound.current,
        interactionSource = interactionSource,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * 按钮四态视觉样式集：[ButtonState] → [ButtonStateStyle] 的不可变查找表。
 *
 * 容器与内容色的解析完全交给 [get]，状态推导交给 [ButtonState.resolve]——
 * 本类只承载"每个状态长什么样"，不承载交互逻辑。
 *
 * [tone] 为容器精灵的染色色板（按钮的语义色系，四态共享——状态差异由精灵本身表达，
 * tone 是叠加语义）；[selectedOutlineColor] 为悬停/聚焦态 outline 层的染色（描边高亮）。
 * 两者默认均由 [ButtonDefaults.colors] 解析为主题值。
 *
 * 默认集取 [ButtonDefaults.colors]；自定义时用 data class `copy` 精准微调
 * （如只换 disabled 的内容色：`colors.copy(disabled = colors.disabled.copy(contentColor = ...))`）。
 */
@Immutable
data class ButtonColors(
    val contentColor: UiStateColor,
    val tone: ColorTone,
    val selectedOutlineColor: Color,
)

object ButtonDefaults {

    inline val meta get() = SokitsuThemeMeta.button

    val LocalPressSound = compositionLocalOf<SoundInstance?> { SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f) }

    /**
     * 默认按钮样式集：按 [ButtonMeta] 四态精灵 + 按组件 token 映射表解析出的容器与内容色。
     *
     * 四个参数**全部默认为未指定**（[ColorTone.Unspecified] / [Color.Unspecified]），
     * 语义是"调用方没意见，请按 [ButtonTokens] 映射表结合当前主题解析"。
     * 回退顺序：`调用点传参` > [LocalSokitsuTone] 作用域 > [ButtonTokens] > [ColorScheme]。
     *
     * - [tone] 容器染色色板 → [ButtonTokens.Container]（默认 primary）
     * - [contentColor] 内容色 → 由**解析后的容器色板**配对推导（[ColorTone.contentColor]）。
     *   这样作用域切到 secondary 时内容色会自动跟着变 onSecondary；
     *   若色板不属于主题已知槽位，则回退 [ButtonTokens.Content]
     * - [disabledContentColor] → [ButtonTokens.DisabledContent] 压
     *   [ButtonTokens.DisabledContentOpacity]（默认 onSurface @ 38%）
     * - [selectedOutlineColor] 悬停/聚焦外框 → 未指定时取容器色板 base 的对比色
     *
     * 显式传值即完全接管该槽位，不会被再次覆盖（见 [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolveFaded]）。
     */
    @Composable
    fun colors(
        tone: ColorTone = ColorTone.Unspecified,
        selectedOutlineColor: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
        disabledContentColor: Color = Color.Unspecified,
    ): ButtonColors {
        val resolvedTone = tone.resolve(ButtonTokens.Container)
        val resolvedContent = contentColor.takeOrElse { resolvedTone.contentColor() }
        val resolvedDisabledContent = disabledContentColor.resolveFaded(
            ButtonTokens.DisabledContent,
            ButtonTokens.DisabledContentOpacity,
        )
        return ButtonColors(
            contentColor = UiStateColor(
                normal = resolvedContent,
                pressed = resolvedContent,
                focused = resolvedContent,
                disabled = resolvedDisabledContent
            ),
            tone = resolvedTone,
            selectedOutlineColor = selectedOutlineColor.takeOrElse {
                resolvedTone.base.contrasting()
            }
        )
    }

    fun sprite(): UiStateSprite = meta.sprite.toSprite()

    /**
     * 按钮最小尺寸：来自全局 [SokitsuThemeMeta] 的 button 段（**单位 dp**，资源包可覆盖）。
     */
    val minSize: DpSize get() = meta.minSize

    /**
     * 内容内边距（水平 = [ButtonMeta.padding]，垂直 = [ButtonMeta.paddingVertical]）。
     * 与最小尺寸同源于主题 meta 的 button 段。
     */
    val contentPadding: PaddingValues get() = meta.padding

}
