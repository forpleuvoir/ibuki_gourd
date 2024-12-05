package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.input.MouseCursorMapping

object WidgetCustomData {

    const val MOUSE_OVER_CURSOR = "mouseOverCursor"

    @Suppress("UNCHECKED_CAST")
    val IGWidget.mouseOverCursor: MouseCursor?
        get() {
            return runCatching { customData[MOUSE_OVER_CURSOR] as? MouseCursorMapping<IGWidget> }
                .getOrNull()
                ?.invoke(this)
                ?: customData[MOUSE_OVER_CURSOR] as? MouseCursor
        }

}