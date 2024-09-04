package moe.forpleuvoir.ibukigourd.gui.widget.theme

import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.BUTTON_DISABLED_1
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.BUTTON_DISABLED_2
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.BUTTON_DISABLED_3
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.BUTTON_HOVERED_1
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.BUTTON_HOVERED_2
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.BUTTON_HOVERED_3
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.BUTTON_IDLE_1
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.BUTTON_IDLE_2
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.BUTTON_IDLE_3
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.BUTTON_PRESSED_1
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.BUTTON_PRESSED_2
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.BUTTON_PRESSED_3
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.LOCK_ON_DISABLED
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.LOCK_ON_HOVERED
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.LOCK_ON_IDLE
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.LOCK_ON_PRESSED
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.SCROLLER_BACKGROUND
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.SCROLLER_BAR_DISABLED
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.SCROLLER_BAR_HOVERED
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.SCROLLER_BAR_IDLE
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.SCROLLER_BAR_PRESSED
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.UNLOCK_DISABLED
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.UNLOCK_HOVERED
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.UNLOCK_IDLE
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures.UNLOCK_PRESSED
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGPressableWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget

interface PressableTheme : WidgetTheme {

    val pressed: WidgetTexture


    companion object {
        private data class PressableThemeImpl(
            override val idle: WidgetTexture,
            override val hovered: WidgetTexture,
            override val disabled: WidgetTexture,
            override val pressed: WidgetTexture
        ) : PressableTheme

        fun of(
            idle: WidgetTexture,
            hovered: WidgetTexture,
            disabled: WidgetTexture,
            pressed: WidgetTexture
        ): PressableTheme = PressableThemeImpl(idle, hovered, disabled, pressed)

        val Button1 = of(BUTTON_IDLE_1, BUTTON_HOVERED_1, BUTTON_DISABLED_1, BUTTON_PRESSED_1)

        val Button2 = of(BUTTON_IDLE_2, BUTTON_HOVERED_2, BUTTON_DISABLED_2, BUTTON_PRESSED_2)

        val Button3 = of(BUTTON_IDLE_3, BUTTON_HOVERED_3, BUTTON_DISABLED_3, BUTTON_PRESSED_3)

        val LOCK = of(LOCK_ON_IDLE, LOCK_ON_HOVERED, LOCK_ON_DISABLED, LOCK_ON_PRESSED)

        val UNLOCK = of(UNLOCK_IDLE, UNLOCK_HOVERED, UNLOCK_DISABLED, UNLOCK_PRESSED)

        val ScrollerBar = of(SCROLLER_BAR_IDLE, SCROLLER_BAR_HOVERED, SCROLLER_BAR_DISABLED, SCROLLER_BAR_PRESSED)

        val ScrollerBackground = of(SCROLLER_BACKGROUND, SCROLLER_BACKGROUND, SCROLLER_BACKGROUND, SCROLLER_BACKGROUND)

    }

}

fun IGPressableWidget.theme(theme: PressableTheme): WidgetTexture =
    status(theme.disabled, theme.idle, theme.pressed, theme.pressed)

fun IGWidget.theme(
    theme: PressableTheme,
    active: Boolean = this.active,
    hover: Boolean = this.wasMouseOver || this.isFocused,
    pressed: Boolean
): WidgetTexture {
    return if (active) {
        if (pressed) theme.pressed
        else if (hover) theme.hovered
        else theme.idle
    } else theme.disabled
}