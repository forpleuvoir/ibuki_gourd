package moe.forpleuvoir.ibukigourd.ui.sokitsu

import androidx.compose.runtime.Immutable
import moe.forpleuvoir.ibukigourd.ui.sokitsu.UiState.*
import moe.forpleuvoir.ibukigourd.util.codec.ibukigourdIdentifier
import moe.forpleuvoir.nebula.serialization.codec.Codec
import net.minecraft.resources.Identifier

@Immutable
data class UiStateSprite(
    val normal: Identifier,
    val pressed: Identifier,
    val focused: Identifier,
    val disabled: Identifier
) {
    companion object : Codec<UiStateSprite> by Codec.create<UiStateSprite>()
        .field(UiStateSprite::normal).codec(Codec.ibukigourdIdentifier)
        .field(UiStateSprite::pressed).codec(Codec.ibukigourdIdentifier)
        .field(UiStateSprite::focused).codec(Codec.ibukigourdIdentifier)
        .field(UiStateSprite::disabled).codec(Codec.ibukigourdIdentifier)
        .build(::UiStateSprite)


    operator fun get(state: UiState):Identifier = when (state) {
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