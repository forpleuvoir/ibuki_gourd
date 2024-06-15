package moe.forpleuvoir.ibukigourd.gui.widget.button

import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTexture
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.BUTTON_DISABLED_1
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.BUTTON_DISABLED_2
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.BUTTON_HOVERED_1
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.BUTTON_HOVERED_2
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.BUTTON_IDLE_1
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.BUTTON_IDLE_2
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.BUTTON_PRESSED_1
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.BUTTON_PRESSED_2
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.SCROLLER_BAR_DISABLED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.SCROLLER_BAR_HOVERED
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.SCROLLER_BAR_IDLE
import moe.forpleuvoir.ibukigourd.gui.texture.WidgetTextures.SCROLLER_BAR_PRESSED

interface ButtonTheme {

    val idle: WidgetTexture

    val hovered: WidgetTexture

    val pressed: WidgetTexture

    val disabled: WidgetTexture

}

enum class ButtonThemes(
    override val idle: WidgetTexture,
    override val hovered: WidgetTexture,
    override val pressed: WidgetTexture,
    override val disabled: WidgetTexture
) : ButtonTheme {
    Button1(BUTTON_IDLE_1, BUTTON_HOVERED_1, BUTTON_PRESSED_1, BUTTON_DISABLED_1),
    Button2(BUTTON_IDLE_2, BUTTON_HOVERED_2, BUTTON_PRESSED_2, BUTTON_DISABLED_2),
    ScrollerBar(SCROLLER_BAR_IDLE, SCROLLER_BAR_HOVERED, SCROLLER_BAR_PRESSED, SCROLLER_BAR_DISABLED)
}