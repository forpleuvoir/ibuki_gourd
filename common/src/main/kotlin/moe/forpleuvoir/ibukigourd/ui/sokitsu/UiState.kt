package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import moe.forpleuvoir.ibukigourd.ui.sokitsu.UiState.*
import moe.forpleuvoir.ibukigourd.ui.sokitsu.texture.atlas.SokitsuSprite
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.ColorTone
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.SokitsuThemeMeta
import moe.forpleuvoir.ibukigourd.ui.sokitsu.theme.uiSprite
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier

@Immutable
data class UiStateIdentifier(
    override val normal: Identifier,
    override val pressed: Identifier,
    override val focused: Identifier,
    override val disabled: Identifier
) : UiStateValue<Identifier> {

    companion object : Codec<UiStateIdentifier> by Codec.create<UiStateIdentifier>()
        .field(UiStateIdentifier::normal).codec(Codec.ibukigourdIdentifier)
        .field(UiStateIdentifier::pressed).codec(Codec.ibukigourdIdentifier)
        .field(UiStateIdentifier::focused).codec(Codec.ibukigourdIdentifier)
        .field(UiStateIdentifier::disabled).codec(Codec.ibukigourdIdentifier)
        .build(::UiStateIdentifier)

}

fun UiStateIdentifier.toSprite(spriteProvider: (Identifier) -> SokitsuSprite = SokitsuThemeMeta::uiSprite) = UiStateSprite(
    normal = normal.toSprite(spriteProvider),
    pressed = pressed.toSprite(spriteProvider),
    focused = focused.toSprite(spriteProvider),
    disabled = disabled.toSprite(spriteProvider)
)

inline fun Identifier.toSprite(spriteProvider: (Identifier) -> SokitsuSprite = SokitsuThemeMeta::uiSprite) = spriteProvider(this)

@Immutable
data class UiStateSprite(
    override val normal: SokitsuSprite,
    override val pressed: SokitsuSprite,
    override val focused: SokitsuSprite,
    override val disabled: SokitsuSprite
) : UiStateValue<SokitsuSprite>

@Immutable
data class UiStateColor(
    override val normal: Color,
    override val pressed: Color,
    override val focused: Color,
    override val disabled: Color
) : UiStateValue<Color>

@Immutable
data class UiStateColorTone(
    override val normal: ColorTone,
    override val pressed: ColorTone,
    override val focused: ColorTone,
    override val disabled: ColorTone
) : UiStateValue<ColorTone>

interface UiStateValue<T> {
    val normal: T
    val pressed: T
    val focused: T
    val disabled: T

    operator fun get(state: UiState): T = when (state) {
        Normal   -> normal
        Pressed  -> pressed
        Focused  -> focused
        Disabled -> disabled
    }
}

/**
 * 交互状态（由 [resolve] 按优先级推导：disabled > pressed > (hover ∪ focused) > normal）。
 */
enum class UiState {

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
        ): UiState = when {
            !enabled           -> Disabled
            pressed            -> Pressed
            hovered || focused -> Focused
            else               -> Normal
        }
    }
}

