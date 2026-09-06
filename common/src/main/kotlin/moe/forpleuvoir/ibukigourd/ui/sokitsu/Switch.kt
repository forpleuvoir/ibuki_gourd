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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import moe.forpleuvoir.ibukigourd.ui.sokitsu.draw.sokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorTone
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolve
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.resolveFaded
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.takeOrElse
import moe.forpleuvoir.ibukigourd.util.contrasting
import moe.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundEvents
import kotlin.math.roundToInt

/**
 * 开关（Switch）。轨道为静态精灵背景，把手为可滑动精灵，按 [checked] 在轨道上水平滑动。
 *
 * 颜色经 [SwitchColors] 解析（调用点 → 作用域 [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuTone]
 * → [SwitchTokens] → [ColorScheme]）；把手精灵按交互状态切换 normal/press/focus/disable 四态，
 * 轨道为单行精灵（无状态变体）。
 *
 * 把手滑动使用 [animateFloatAsState] 默认 spring 缓动，关闭贴在左、开启贴右。
 *
 * 尺寸：轨道的最小尺寸与把手的尺寸取自 [SwitchDefaults]（资源包可覆盖）；[modifier] 给出的
 * 约束对轨道直接生效。把手位置（内边距与滑动行程）在测量阶段按两者的实测尺寸算出，
 * 空间不足时留白与行程退化为 0。
 *
 * @param checked 是否开启
 * @param onCheckedChange 状态变化回调（null 表示只读展示，仍受 [enabled] 控制不可交互）
 * @param modifier 轨道修饰（尺寸按传入约束收敛，把手位置随之重算）
 * @param enabled 是否可交互
 * @param thumbModifier 把手修饰（在 [SwitchDefaults.thumbMinSize] 基础上叠加）
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
    thumbModifier: Modifier = Modifier,
    thumbSprite: UiStateSprite = SwitchDefaults.thumbSprite(),
    trackSprite: SokitsuSprite = SwitchDefaults.trackSprite(),
    interactionSource: MutableInteractionSource? = null,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val thumbSprite = thumbSprite[UiState.resolve(enabled, pressed, hovered, focused)]

    val clickSound = SwitchDefaults.LocalPressSound.current

    // 把手在轨道最左/最右之间滑动：0 = 关闭(左)，1 = 开启(右)；在摆放阶段读取，动画只使本节点重新摆放
    val fraction = animateFloatAsState(if (checked) 1f else 0f)

    // 轨道的最小尺寸与把手的尺寸，均取自 [SwitchDefaults]
    val trackMinSize = SwitchDefaults.trackMinSize
    val thumbMinSize = SwitchDefaults.thumbMinSize

    // 轨道目标色板（开启/关闭/禁用态）：先算出目标，再整体平滑过渡到目标
    val targetTrackTone = colors.trackColor(enabled, checked)

    // 悬停/聚焦（选中态）：仅 outline 层覆盖为 [SwitchColors.selectedOutlineColor]，
    // 其余层沿用色板自带的 [ColorTone.outline] 档。
    val targetThumbTone = if (hovered || focused) {
        colors.thumbColor(enabled, checked).copy(outline = colors.selectedOutlineColor)
    } else {
        colors.thumbColor(enabled, checked)
    }

    // 色板逐字段平滑过渡，开启/关闭/禁用切换时不瞬变（compose-minecraft 无 [tween]，用默认 spring）
    val trackTone = animateColorTone(targetTrackTone)
    val thumbTone = animateColorTone(targetThumbTone)

    Layout(
        content = {
            // 把手：唯一的子节点
            Box(
                modifier = thumbModifier
                    .defaultMinSize(thumbMinSize.width, thumbMinSize.height)
                    .sokitsuSprite(thumbSprite, thumbTone)
            )
        },
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
            .defaultMinSize(trackMinSize.width, trackMinSize.height)
            .sokitsuSprite(trackSprite, trackTone),
        measurePolicy = { measurables, constraints ->
            // 下限归零：把手尺寸由它自身的 defaultMinSize 决定
            val thumb = measurables[0].measure(constraints.copy(minWidth = 0, minHeight = 0))
            // 收敛进约束：结果不小于 [trackMinSize]，调用方限定时取其限定值
            val width = constraints.constrainWidth(thumb.width)
            val height = constraints.constrainHeight(thumb.height)
            // 垂直留白居中，水平留白与之一致，剩余宽度即滑动行程；空间不足时退化为 0
            val insetY = ((height - thumb.height) / 2).coerceAtLeast(0)
            val insetX = minOf((width - thumb.width) / 2, insetY).coerceAtLeast(0)
            val travel = (width - thumb.width - 2 * insetX).coerceAtLeast(0)
            layout(width, height) {
                thumb.placeRelative(insetX + (travel * fraction.value).roundToInt(), insetY)
            }
        },
    )
}


object SwitchDefaults {

    val LocalPressSound = compositionLocalOf<SoundInstance?> { SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f) }

    inline val meta get() = SokitsuThemeMeta.switch

    /**
     * 开关默认尺寸
     *
     * 与当前 switch 纹理比例匹配；调用方若要整体覆盖尺寸，
     *
     * 可经 [Switch] 的 [modifier] 自行 [androidx.compose.ui.Modifier.size] 控制。
     */
    val trackMinSize: DpSize
        @Composable get() = SokitsuThemeMeta.switch.trackMinSize

    val thumbMinSize: DpSize
        @Composable get() = SokitsuThemeMeta.switch.thumbMinSize

    /**
     * 按 [SwitchState] 取对应把手精灵（已解析为 [SokitsuSprite]）。
     *
     * 状态→精灵的映射集中在此（见 [thumbSpriteId]）。
     */
    fun thumbSprite(): UiStateSprite = meta.thumbSprite.toSprite()

    fun trackSprite(): SokitsuSprite = meta.trackSprite.toSprite()

    /**
     * 默认开关配色集：八个状态色板槽位与选中描边色**全部有默认值**，`SwitchDefaults.colors()` 可无参调用。
     *
     * 参数默认 [ColorTone.Unspecified]，语义是"按 [SwitchTokens] 映射表结合当前主题解析"，
     * 回退顺序：`调用点传参` > [moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.LocalSokitsuTone]
     * 作用域 > [SwitchTokens] > [ColorScheme]。
     *
     * - [selectedOutlineColor] 悬停/聚焦（选中态）把手描边高亮 → 未指定时取**开启态把手**
     *   色板 base 的对比色（[contrasting]），使选中描边色不随开启/关闭漂移。
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

/**
 * 把 [ColorTone] 的五档色（outline/shadow/dark/base/highlight）逐字段用 [animateColorAsState]
 * 平滑过渡，返回动画进行中的色板。用于开关在开启/关闭/禁用态切换时颜色不瞬变。
 *
 * [ColorTone] 是五档数据类，[animateColorAsState] 只能过渡单个 [Color]，因此逐字段动画；
 * compose-minecraft 当前未提供 [androidx.compose.animation.core.tween]，使用默认 spring。
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