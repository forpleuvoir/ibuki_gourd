package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.input.MouseCursorMapping
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object WidgetCustomData {

    private const val MOUSE_OVER_CURSOR_KEY = "mouseOverCursor"

    fun IGWidget.setMouseOverCursor(cursor: MouseCursor) {
        customData[MOUSE_OVER_CURSOR_KEY] = cursor
    }

    fun <W : IGWidget> W.setMouseOverCursorMapping(mapping: MouseCursorMapping<W>) {
        customData[MOUSE_OVER_CURSOR_KEY] = mapping
    }

    @Suppress("UNCHECKED_CAST")
    val IGWidget.mouseOverCursor: MouseCursor?
        get() {
            return runCatching { customData[MOUSE_OVER_CURSOR_KEY] as? MouseCursorMapping<IGWidget> }
                .getOrNull()
                ?.invoke(this)
                ?: customData[MOUSE_OVER_CURSOR_KEY] as? MouseCursor
        }

    //------------ HoverText ------------\\

    private const val HOVER_TEXT_KEY = "hoverText"

    fun IGWidget.setHoverText(text: () -> Text) {
        customData[HOVER_TEXT_KEY] = text
    }

    @Suppress("UNCHECKED_CAST")
    val IGWidget.hoverText: () -> Text?
        get() = customData[HOVER_TEXT_KEY] as? (() -> Text) ?: { null }

    private const val HOVER_TEXT_SHOW_DELAY_KEY = "hoverTextShowDelay"

    val IGWidget.hoverTextShowDelay: Duration
        get() = customData[HOVER_TEXT_SHOW_DELAY_KEY] as? Duration ?: 200.milliseconds

    fun IGWidget.setHoverTextShowDelay(delay: Duration) {
        customData[HOVER_TEXT_SHOW_DELAY_KEY] = delay
    }

    private const val HOVER_TEXT_DIRECTION_KEY = "hoverTextDirection"

    fun IGWidget.setHoverTextDirection(direction: () -> List<Direction>) {
        customData[HOVER_TEXT_DIRECTION_KEY] = direction
    }

    @Suppress("UNCHECKED_CAST")
    val IGWidget.hoverTextDirection: () -> List<Direction>
        get() = customData[HOVER_TEXT_DIRECTION_KEY] as? () -> List<Direction> ?: { Direction.clockwiseFromTop }

    private const val HOVER_TEXT_MARGIN_KEY = "hoverTextMargin"

    fun IGWidget.setHoverTextMargin(margin: () -> Margin) {
        customData[HOVER_TEXT_MARGIN_KEY] = margin
    }

    @Suppress("UNCHECKED_CAST")
    val IGWidget.hoverTextMargin: () -> Margin
        get() = customData[HOVER_TEXT_MARGIN_KEY] as? () -> Margin ?: { Margin(4f) }

    private const val HOVER_TEXT_PADDING_KEY = "hoverTextPadding"

    fun IGWidget.setHoverTextPadding(padding: () -> Padding) {
        customData[HOVER_TEXT_PADDING_KEY] = padding
    }

    @Suppress("UNCHECKED_CAST")
    val IGWidget.hoverTextPadding: () -> Padding
        get() = customData[HOVER_TEXT_PADDING_KEY] as? () -> Padding ?: { Padding(4f) }


    private const val HOVER_TEXT_BG_COLOR_KEY = "hoverTextBgColor"

    fun IGWidget.setHoverTextBGColor(color: () -> ARGBColor) {
        customData[HOVER_TEXT_BG_COLOR_KEY] = color
    }

    @Suppress("UNCHECKED_CAST")
    val IGWidget.hoverTextBGColor: () -> ARGBColor
        get() = customData[HOVER_TEXT_BG_COLOR_KEY] as? () -> ARGBColor ?: { Colors.WHITE }

}