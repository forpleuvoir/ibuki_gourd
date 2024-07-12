package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget

interface WidgetScope<T : IGWidget> : GuiScope<T> {

    companion object {

        fun <T : IGWidget> create(widget: T): WidgetScope<T> = object : WidgetScope<T> {
            override val owner: T get() = widget
        }

        @Suppress("NOTHING_TO_INLINE")
        inline fun <T : IGWidget> T.create(): WidgetScope<T> = create(this)

    }


}