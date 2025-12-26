package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.tip.Tip
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.input.MouseCursorMapping

object WidgetUserData {

    private const val MOUSE_OVER_CURSOR_KEY = "#mouse_over_cursor"

    fun <W : GuiWidget> W.setMouseOverCursorMapping(mapping: MouseCursorMapping<W>) {
        userData[MOUSE_OVER_CURSOR_KEY] = mapping
    }

    @Suppress("UNCHECKED_CAST")
    var GuiWidget.mouseOverCursor: MouseCursor?
        get() {
            return runCatching { userData[MOUSE_OVER_CURSOR_KEY] as? MouseCursorMapping<GuiWidget> }
                .getOrNull()
                ?.invoke(this)
                ?: userData[MOUSE_OVER_CURSOR_KEY] as? MouseCursor
        }
        set(value) {
            value?.let {
                userData[MOUSE_OVER_CURSOR_KEY] = value
            }
        }

    //------------ HoverTip ------------\\

    private const val HOVER_TIP_KEY = "#hover_tip"

    var GuiWidget.hoverTip: Tip?
        get() = userData[HOVER_TIP_KEY] as? Tip
        set(value) {
            value?.let { userData[HOVER_TIP_KEY] = value }
        }

    //------------ Hoverable ------------\\

    private const val HOVERABLE_KEY = "#hoverable"

    @Suppress("UNCHECKED_CAST")
    var GuiWidget.hoverable: (GuiWidget) -> Boolean
        get() = (userData[HOVERABLE_KEY] as? (GuiWidget) -> Boolean) ?: { it.active && it.visible }
        set(value) {
            userData[HOVERABLE_KEY] = value
        }

    val GuiWidget.isHoverable: Boolean get() = hoverable(this)

}