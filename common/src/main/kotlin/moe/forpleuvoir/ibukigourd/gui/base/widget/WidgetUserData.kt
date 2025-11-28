package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.tip.Tip
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.input.MouseCursorMapping

object WidgetUserData {

    private const val MOUSE_OVER_CURSOR_KEY = "#mouse_over_cursor"

    fun GuiWidget.setMouseOverCursor(cursor: MouseCursor) {
        userData[MOUSE_OVER_CURSOR_KEY] = cursor
    }

    fun <W : GuiWidget> W.setMouseOverCursorMapping(mapping: MouseCursorMapping<W>) {
        userData[MOUSE_OVER_CURSOR_KEY] = mapping
    }

    @Suppress("UNCHECKED_CAST")
    val GuiWidget.mouseOverCursor: MouseCursor?
        get() {
            return runCatching { userData[MOUSE_OVER_CURSOR_KEY] as? MouseCursorMapping<GuiWidget> }
                .getOrNull()
                ?.invoke(this)
                ?: userData[MOUSE_OVER_CURSOR_KEY] as? MouseCursor
        }

    //------------ HoverTip ------------\\

    private const val HOVER_TIP_KEY = "#hover_tip"

    fun GuiWidget.setHoverTip(tip: Tip) {
        userData[HOVER_TIP_KEY] = tip
    }

    @Suppress("UNCHECKED_CAST")
    val GuiWidget.hoverTip: Tip?
        get() = userData[HOVER_TIP_KEY] as? Tip

}