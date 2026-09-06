package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.compositionLocalOf
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
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorTone
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolveFaded
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.takeOrElse
import moe.forpleuvoir.ibukigourd.util.contrasting
import net.minecraft.resources.Identifier
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundEvents
import moe.forpleuvoir.ibukigourd.util.mc

/**
 * 开关（Toggle）。轨道为静态精灵背景，把手为可滑动精灵，按 [checked] 在轨道上水平滑动。
 *
 * 颜色经 [SwitchColors] 解析（调用点 → 作用域 [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuTone]
 * → [SwitchTokens] → [ColorScheme]）；把手精灵按交互状态切换 normal/press/focus/disable 四态，
 * 轨道为单行精灵（无状态变体）。
 *
 * 把手滑动使用 [animateFloatAsState] 默认 spring 缓动（compose-minecraft 当前未提供
 * [androidx.compose.animation.core.tween]），关闭贴在左、开启贴右。
 *
 * @param checked 是否开启
 * @param onCheckedChange 状态变化回调（null 表示只读展示，仍受 [enabled] 控制不可交互）
 * @param modifier 外部修饰（本组件已按轨道尺寸定好，调用方一般不要再设尺寸）
 * @param enabled 是否可交互
 * @param colors 配色集，默认 [SwitchDefaults.colors]
 * @param interactionSource 交互源，不传则内部新建
 */
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    // 把手四态精灵经 [SwitchState] 统一推导后从 [SwitchDefaults] 取图，组合方法不写死状态分支
    val thumbSprite = SwitchDefaults.thumbSprite(SwitchState.resolve(enabled, pressed, hovered, focused))

    // 点击音效（参考 [Button] 实现）：默认播放 UI 按钮音，作用域内可用 [SwitchDefaults.LocalPressSound] 覆盖
    val clickSound = SwitchDefaults.LocalPressSound.current

    // 把手按 checked 在 [uncheckedX, checkedX] 间滑动：0 = 关闭(左)，1 = 开启(右)
    val fraction by animateFloatAsState(if (checked) 1f else 0f)

    // 尺寸取自 [SwitchDefaults]（默认与当前纹理比例匹配），不在此写死
    val trackWidth = SwitchDefaults.trackWidth
    val trackHeight = SwitchDefaults.trackHeight
    val thumbSize = SwitchDefaults.thumbSize
    // 垂直内边距 = (轨道高 - 把手高) / 2，水平/垂直统一取同一 inset 保证居中
    val inset = (trackHeight - thumbSize) / 2
    val uncheckedX = inset
    val checkedX = trackWidth - thumbSize - inset

    // 轨道目标色板（开启/关闭/禁用态）：先算出目标，再整体平滑过渡到目标
    val targetTrackTone = colors.trackColor(enabled, checked)

    // 悬停/聚焦（选中态）：仅 outline 层覆盖为 [SwitchColors.selectedOutlineColor]（描边高亮），
    // 其余层保持色板明暗结构——与 [Button] 聚焦描边同款机制；
    // 其余状态一律沿用色板自带的 [ColorTone.outline] 档，不单独配置 outline。
    val targetThumbTone = if (hovered || focused) {
        colors.thumbColor(enabled, checked).copy(outline = colors.selectedOutlineColor)
    } else {
        colors.thumbColor(enabled, checked)
    }

    // 颜色平滑过渡：开启/关闭/禁用切换时避免瞬变（compose-minecraft 无 [tween]，用默认 spring，
    // 与 [animateFloatAsState] 的位置动画一致）
    val trackTone = animateColorTone(targetTrackTone)
    val thumbTone = animateColorTone(targetThumbTone)

    Box(
        modifier = modifier
            .semantics { role = Role.Switch }
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = {
                    clickSound?.let { sound -> mc.soundManager.play(sound) }
                    onCheckedChange?.invoke(!checked)
                },
            )
            .defaultMinSize(trackWidth, trackHeight)
            .sokitsuSprite(sprite(SwitchDefaults.meta.trackSprite), trackTone)
    ) {
        Box(
            modifier = Modifier
                .offset(x = uncheckedX + (checkedX - uncheckedX) * fraction, y = inset)
                .size(thumbSize)
                .sokitsuSprite(thumbSprite, thumbTone)
        )
    }
}

/**
 * 开关交互状态（由 [SwitchState.resolve] 按优先级推导：disabled > pressed > (hover ∪ focused) > normal）。
 */
enum class SwitchState {

    Normal, Pressed, Focused, Disabled;

    companion object {

        /**
         * 由交互布尔推导当前状态。
         *
         * hover 无独立视觉资源，与 focused 共用 [Focused]（合并只发生在这里，
         * 精灵映射与状态机解耦）。
         */
        fun resolve(
            enabled: Boolean,
            pressed: Boolean,
            hovered: Boolean,
            focused: Boolean,
        ): SwitchState = when {
            !enabled           -> Disabled
            pressed            -> Pressed
            hovered || focused -> Focused
            else               -> Normal
        }
    }
}

object SwitchDefaults {

    val LocalPressSound = compositionLocalOf<SoundInstance?> { SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f) }

    inline val meta get() = SokitsuThemeMeta.switch

    /** 开关纹理（把手四态 + 轨道）来自全局 [SokitsuThemeMeta] 的 switch 段（资源包可覆盖）。 */

    /**
     * 开关默认尺寸：来自全局 [SokitsuThemeMeta] 的 switch 段（**单位 dp**，资源包可覆盖）。
     * 与当前 switch 纹理比例匹配；调用方若要整体覆盖尺寸，
     * 可经 [Switch] 的 [modifier] 自行 [androidx.compose.ui.Modifier.size] 控制。
     */
    val trackWidth: Dp
        @Composable get() = SokitsuThemeMeta.switch.trackWidth.dp
    val trackHeight: Dp
        @Composable get() = SokitsuThemeMeta.switch.trackHeight.dp
    val thumbSize: Dp
        @Composable get() = SokitsuThemeMeta.switch.thumbSize.dp

    /**
     * 按 [SwitchState] 取对应把手精灵（已解析为 [SokitsuSprite]）。
     *
     * 状态→精灵的映射集中在此（见 [thumbSpriteId]），组合方法不写死 `when`——
     * 与 [Button] 的 [moe.forpleuvoir.ibukigourd.ui.sokitsu.ButtonState] +
     * [moe.forpleuvoir.ibukigourd.ui.sokitsu.ButtonDefaults.colors] 思路一致。
     */
    fun thumbSprite(state: SwitchState): SokitsuSprite = sprite(thumbSpriteId(state))

    private fun thumbSpriteId(state: SwitchState): Identifier = when (state) {
        SwitchState.Normal   -> meta.thumbNormalSprite
        SwitchState.Pressed  -> meta.thumbPressedSprite
        SwitchState.Focused  -> meta.thumbFocusedSprite
        SwitchState.Disabled -> meta.thumbDisabledSprite
    }

    /**
     * 默认开关配色集：八个状态色板槽位与选中描边色**全部有默认值**，`SwitchDefaults.colors()` 可无参调用。
     *
     * 参数默认 [ColorTone.Unspecified]，语义是"按 [SwitchTokens] 映射表结合当前主题解析"，
     * 回退顺序：`调用点传参` > [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuTone]
     * 作用域 > [SwitchTokens] > [ColorScheme]。
     *
     * - [selectedOutlineColor] 悬停/聚焦（选中态）把手描边高亮 → 未指定时取**开启态把手**
     *   色板 base 的对比色（[contrasting]），与 [moe.forpleuvoir.ibukigourd.ui.sokitsu.ButtonDefaults.colors]
     *   同款，保证选中描边色恒定、不随开启/关闭漂移。
     *
     * 启用态走 [resolve]，禁用态走 [resolveFaded] 额外压一层不透明度
     * （把手 38%、轨道 12%，沿用 Material3）。
     */
    @Composable
    fun colors(
        checkedThumbColor: ColorTone = ColorTone.Unspecified,
        checkedTrackColor: ColorTone = ColorTone.Unspecified,
        uncheckedThumbColor: ColorTone = ColorTone.Unspecified,
        uncheckedTrackColor: ColorTone = ColorTone.Unspecified,
        disabledCheckedThumbColor: ColorTone = ColorTone.Unspecified,
        disabledCheckedTrackColor: ColorTone = ColorTone.Unspecified,
        disabledUncheckedThumbColor: ColorTone = ColorTone.Unspecified,
        disabledUncheckedTrackColor: ColorTone = ColorTone.Unspecified,
        selectedOutlineColor: Color = Color.Unspecified,
    ): SwitchColors {
        val resolvedCheckedThumb = checkedThumbColor.resolve(SwitchTokens.CheckedThumb)
        return SwitchColors(
            checkedThumbColor = resolvedCheckedThumb,
            checkedTrackColor = checkedTrackColor.resolve(SwitchTokens.CheckedTrack),
            uncheckedThumbColor = uncheckedThumbColor.resolve(SwitchTokens.UncheckedThumb),
            uncheckedTrackColor = uncheckedTrackColor.resolve(SwitchTokens.UncheckedTrack),
            disabledCheckedThumbColor = disabledCheckedThumbColor.resolveFaded(
                SwitchTokens.DisabledThumb,
                SwitchTokens.DisabledThumbOpacity,
            ),
            disabledCheckedTrackColor = disabledCheckedTrackColor.resolveFaded(
                SwitchTokens.DisabledTrack,
                SwitchTokens.DisabledTrackOpacity,
            ),
            disabledUncheckedThumbColor = disabledUncheckedThumbColor.resolveFaded(
                SwitchTokens.DisabledThumb,
                SwitchTokens.DisabledThumbOpacity,
            ),
            disabledUncheckedTrackColor = disabledUncheckedTrackColor.resolveFaded(
                SwitchTokens.DisabledTrack,
                SwitchTokens.DisabledTrackOpacity,
            ),
            selectedOutlineColor = selectedOutlineColor.takeOrElse {
                resolvedCheckedThumb.base.contrasting()
            },
        )
    }

}


private fun sprite(textureId: Identifier): SokitsuSprite =
    SokitsuAtlasManager.sprite(SokitsuThemeMeta.uiAtlas, textureId)


/**
 * 把 [ColorTone] 的五档色（outline/shadow/dark/base/highlight）逐字段用 [animateColorAsState]
 * 平滑过渡，返回动画进行中的色板。用于开关在开启/关闭/禁用态切换时颜色不瞬变。
 *
 * [ColorTone] 是自定义的五档数据类，[animateColorAsState] 只能过渡单个 [Color]，因此逐字段动画；
 * compose-minecraft 当前未提供 [androidx.compose.animation.core.tween]，使用默认 spring，
 * 与 [Switch] 中 [animateFloatAsState] 的位置动画节奏一致。
 */
@Composable
private fun animateColorTone(tone: ColorTone): ColorTone = ColorTone(
    outline = animateColorAsState(tone.outline).value,
    shadow = animateColorAsState(tone.shadow).value,
    dark = animateColorAsState(tone.dark).value,
    base = animateColorAsState(tone.base).value,
    highlight = animateColorAsState(tone.highlight).value,
)


@Immutable
class SwitchColors(
    val checkedThumbColor: ColorTone,
    val checkedTrackColor: ColorTone,
    val uncheckedThumbColor: ColorTone,
    val uncheckedTrackColor: ColorTone,
    val disabledCheckedThumbColor: ColorTone,
    val disabledCheckedTrackColor: ColorTone,
    val disabledUncheckedThumbColor: ColorTone,
    val disabledUncheckedTrackColor: ColorTone,

    /**
     * 悬停/聚焦（选中态）把手描边高亮色，仅覆盖把手 [ColorTone.outline] 层。
     * 默认由 [SwitchDefaults.colors] 解析为开启态把手色板 base 的对比色，
     * 也可由调用点显式传入完全接管。
     */
    val selectedOutlineColor: Color,
) {
    /**
     * 返回副本，可选择性覆盖某些值。
     *
     * **约定**：传入 [ColorTone.Unspecified] 表示"沿用源值"，与 Material3 的
     * `Color.Unspecified` 语义一致，也与 [SwitchDefaults.colors] 的默认参数语义一致。
     * 因此 `colors.copy(checkedThumbColor = ColorTone.Unspecified)` 等价于不改动该项。
     *
     * 实现上逐项 [takeOrElse] 回落，所以已解析的配色被 copy 后仍是已解析的，
     * 不会退化成未指定。
     */
    fun copy(
        checkedThumbColor: ColorTone = this.checkedThumbColor,
        checkedTrackColor: ColorTone = this.checkedTrackColor,
        uncheckedThumbColor: ColorTone = this.uncheckedThumbColor,
        uncheckedTrackColor: ColorTone = this.uncheckedTrackColor,
        disabledCheckedThumbColor: ColorTone = this.disabledCheckedThumbColor,
        disabledCheckedTrackColor: ColorTone = this.disabledCheckedTrackColor,
        disabledUncheckedThumbColor: ColorTone = this.disabledUncheckedThumbColor,
        disabledUncheckedTrackColor: ColorTone = this.disabledUncheckedTrackColor,
        selectedOutlineColor: Color = this.selectedOutlineColor,
    ) =
        SwitchColors(
            checkedThumbColor.takeOrElse { this.checkedThumbColor },
            checkedTrackColor.takeOrElse { this.checkedTrackColor },
            uncheckedThumbColor.takeOrElse { this.uncheckedThumbColor },
            uncheckedTrackColor.takeOrElse { this.uncheckedTrackColor },
            disabledCheckedThumbColor.takeOrElse { this.disabledCheckedThumbColor },
            disabledCheckedTrackColor.takeOrElse { this.disabledCheckedTrackColor },
            disabledUncheckedThumbColor.takeOrElse { this.disabledUncheckedThumbColor },
            disabledUncheckedTrackColor.takeOrElse { this.disabledUncheckedTrackColor },
            selectedOutlineColor,
        )

    /**
     * Represents the color used for the switch's thumb, depending on [enabled] and [checked].
     *
     * @param enabled whether the [Switch] is enabled or not
     * @param checked whether the [Switch] is checked or not
     */
    @Stable
    internal fun thumbColor(enabled: Boolean, checked: Boolean): ColorTone =
        if (enabled) {
            if (checked) checkedThumbColor else uncheckedThumbColor
        } else {
            if (checked) disabledCheckedThumbColor else disabledUncheckedThumbColor
        }

    /**
     * Represents the color used for the switch's track, depending on [enabled] and [checked].
     *
     * @param enabled whether the [Switch] is enabled or not
     * @param checked whether the [Switch] is checked or not
     */
    @Stable
    internal fun trackColor(enabled: Boolean, checked: Boolean): ColorTone =
        if (enabled) {
            if (checked) checkedTrackColor else uncheckedTrackColor
        } else {
            if (checked) disabledCheckedTrackColor else disabledUncheckedTrackColor
        }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is SwitchColors) return false

        if (checkedThumbColor != other.checkedThumbColor) return false
        if (checkedTrackColor != other.checkedTrackColor) return false
        if (uncheckedThumbColor != other.uncheckedThumbColor) return false
        if (uncheckedTrackColor != other.uncheckedTrackColor) return false
        if (disabledCheckedThumbColor != other.disabledCheckedThumbColor) return false
        if (disabledCheckedTrackColor != other.disabledCheckedTrackColor) return false
        if (disabledUncheckedThumbColor != other.disabledUncheckedThumbColor) return false
        if (disabledUncheckedTrackColor != other.disabledUncheckedTrackColor) return false
        if (selectedOutlineColor != other.selectedOutlineColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = checkedThumbColor.hashCode()
        result = 31 * result + checkedTrackColor.hashCode()
        result = 31 * result + uncheckedThumbColor.hashCode()
        result = 31 * result + uncheckedTrackColor.hashCode()
        result = 31 * result + disabledCheckedThumbColor.hashCode()
        result = 31 * result + disabledCheckedTrackColor.hashCode()
        result = 31 * result + disabledUncheckedThumbColor.hashCode()
        result = 31 * result + disabledUncheckedTrackColor.hashCode()
        result = 31 * result + selectedOutlineColor.hashCode()
        return result
    }
}