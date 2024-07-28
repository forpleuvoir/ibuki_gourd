package moe.forpleuvoir.ibukigourd.gui.widget.theme

import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget

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

    }
}

fun IGWidget.theme(theme: WidgetTheme): WidgetTexture {
    return if (active) {
        if (this.wasMouseOver || this.isFocused) theme.hovered
        else theme.idle
    } else theme.disabled
}

