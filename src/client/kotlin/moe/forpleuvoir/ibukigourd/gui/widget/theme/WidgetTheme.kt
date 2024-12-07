package moe.forpleuvoir.ibukigourd.gui.widget.theme

import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures

interface WidgetTheme {

    val idle: WidgetTexture

    val hovered: WidgetTexture

    val disabled: WidgetTexture

    companion object {

        private data class WidgetThemeImpl(
            override val idle: WidgetTexture,
            override val hovered: WidgetTexture,
            override val disabled: WidgetTexture
        ) : WidgetTheme

        private fun of(
            idle: WidgetTexture,
            hovered: WidgetTexture,
            disabled: WidgetTexture
        ): WidgetTheme = WidgetThemeImpl(idle, hovered, disabled)

        val ListLayout = of(WidgetTextures.LIST_BACKGROUND, WidgetTextures.LIST_BACKGROUND, WidgetTextures.LIST_BACKGROUND)

        val TextInput = of(WidgetTextures.TEXT_INPUT, WidgetTextures.TEXT_SELECTED_INPUT, WidgetTextures.TEXT_INPUT)

    }
}

fun IGWidget.theme(
    theme: WidgetTheme,
    active: Boolean = this.active,
    hovered: Boolean = this.wasMouseOver || this.isFocused,
): WidgetTexture {
    return if (active) {
        if (hovered) theme.hovered
        else theme.idle
    } else theme.disabled
}

