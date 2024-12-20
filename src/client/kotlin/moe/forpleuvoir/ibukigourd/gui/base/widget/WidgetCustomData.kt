package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.tip.HoverTip
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.input.MouseCursorMapping

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

    //------------ HoverTip ------------\\

    private const val HOVER_TIP_KEY = "hoverTip"

    fun IGWidget.setHoverTip(tip: HoverTip) {
        customData[HOVER_TIP_KEY] = tip
    }

    @Suppress("UNCHECKED_CAST")
    val IGWidget.hoverTip: HoverTip?
        get() = customData[HOVER_TIP_KEY] as? HoverTip

}