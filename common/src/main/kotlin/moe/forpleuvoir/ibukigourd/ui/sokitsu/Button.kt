package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuAtlasManager
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.*
import moe.forpleuvoir.ibukigourd.util.contrasting
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvents

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.colors(),
    contentPadding: PaddingValues = ButtonDefaults.contentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val state = ButtonState.resolve(enabled, pressed, hovered, focused)
    val style = colors[state]

    // 悬停/聚焦：仅 outline 层覆盖为 selectedOutlineColor（描边高亮），其余层保持色板明暗结构
    val tone = when (state) {
        ButtonState.Focused -> colors.tone.copy(outline = colors.selectedOutlineColor)
        else                -> colors.tone
    }

    val clickSound = ButtonDefaults.LocalPressSound.current

    // 内容色经 LocalContentColor 下发，按钮内的 Text/Icon 自动取到当前状态的内容色
    ProvideContentColorTextStyle(
        contentColor = style.contentColor,
        textStyle = SokitsuTheme.typography.button
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .semantics { role = Role.Button }
                .defaultMinSize(ButtonDefaults.minWidth, ButtonDefaults.minHeight)
                .sokitsuSprite(sprite = style.sprite, tone = tone)
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    enabled = enabled,
                    onClick = {
                        clickSound?.let { sound -> mc.soundManager.play(sound) }
                        onClick()
                    }
                )
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * 按钮交互状态（由 [resolve] 按优先级推导：disabled > pressed > (hover ∪ focused) > normal）。
 */
enum class ButtonState {

    Normal, Pressed, Focused, Disabled;

    companion object {

        /**
         * 由交互布尔推导当前状态。
         *
         * hover 无独立视觉资源，与 focused 共用 [Focused]（合并只发生在这里，
         * 样式数据与状态机解耦）。
         */
        fun resolve(
            enabled: Boolean,
            pressed: Boolean,
            hovered: Boolean,
            focused: Boolean,
        ): ButtonState = when {
            !enabled           -> Disabled
            pressed            -> Pressed
            hovered || focused -> Focused
            else               -> Normal
        }
    }
}

/**
 * 单个交互状态的按钮视觉：容器精灵 + 内容色。
 *
 * [sprite] 由 [sokitsuSprite] 按 Sokitsu 管线渲染（多图层 + 主题 tone 染色）；
 * [contentColor] 经 Button 内 [LocalContentColor] 下发给内容（文字/图标）。
 */
@Immutable
data class ButtonStateStyle(
    val sprite: SokitsuSprite,
    val contentColor: Color,
)

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
    val normal: ButtonStateStyle,
    val pressed: ButtonStateStyle,
    val focused: ButtonStateStyle,
    val disabled: ButtonStateStyle,
    val tone: ColorTone,
    val selectedOutlineColor: Color,
) {

    /** 取某状态的视觉样式。 */
    operator fun get(state: ButtonState): ButtonStateStyle = when (state) {
        ButtonState.Normal   -> normal
        ButtonState.Pressed  -> pressed
        ButtonState.Focused  -> focused
        ButtonState.Disabled -> disabled
    }
}


object ButtonDefaults {

    /** 按钮默认背景纹理（texture/sokitsu/ui/button.aseprite → <ns>:ui/button），位于 [SokitsuAtlasManager.UI_ATLAS_ID] 通用 UI 图集。 */
    val textureId: Identifier = identifier("ui/button")

    /** 按钮按下态背景（texture/sokitsu/ui/button.press.aseprite → <ns>:ui/button.press）。 */
    val pressTextureId: Identifier = identifier("ui/button.press")

    /** 按钮聚焦态背景（texture/sokitsu/ui/button.focus.aseprite → <ns>:ui/button.focus）。 */
    val focusTextureId: Identifier = identifier("ui/button.focus")

    /** 按钮禁用态背景（texture/sokitsu/ui/button.disable.aseprite → <ns>:ui/button.disable）。 */
    val disableTextureId: Identifier = identifier("ui/button.disable")

    val LocalPressSound = compositionLocalOf<SoundInstance?> { SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f) }

    /**
     * 默认按钮样式集：`ui/button` 四态精灵 + 按组件 token 映射表解析出的容器与内容色。
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
            normal = ButtonStateStyle(sprite(textureId), resolvedContent),
            pressed = ButtonStateStyle(sprite(pressTextureId), resolvedContent),
            focused = ButtonStateStyle(sprite(focusTextureId), resolvedContent),
            disabled = ButtonStateStyle(sprite(disableTextureId), resolvedDisabledContent),
            tone = resolvedTone,
            selectedOutlineColor = selectedOutlineColor.takeOrElse {
                resolvedTone.base.contrasting()
            },
        )
    }

    private fun sprite(textureId: Identifier): SokitsuSprite =
        SokitsuAtlasManager.sprite(SokitsuAtlasManager.UI_ATLAS_ID, textureId)

    val minWidth: Dp = 54.dp
    val minHeight: Dp = 54.dp

    val contentPadding: PaddingValues
        @Composable @ReadOnlyComposable
        get() = LocalButtonContentPadding.current

    val LocalButtonContentPadding = staticCompositionLocalOf {
        PaddingValues(12.dp, 8.dp)
    }

}
