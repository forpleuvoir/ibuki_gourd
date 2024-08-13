package moe.forpleuvoir.ibukigourd.gui.base.scope

import moe.forpleuvoir.ibukigourd.gui.base.GuiDslMark
import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainer

@GuiDslMark
fun interface GuiScope<T : Any> {
    fun owner(): T

    companion object {

        @JvmName("createScope")
        fun <T : Any> create(screen: T) = GuiScope { screen }

        @Suppress("NOTHING_TO_INLINE")
        inline fun <T : Any> T.create(): GuiScope<T> = create(this)

        fun <W : IGWidget> GuiScope<out WidgetContainer>.addWidgetChild(child: W) = owner().addWidgetChild(child)

        fun <W : IGWidget> GuiScope<out WidgetContainer>.addWidgetChild(child: W, scope: W.() -> Unit) = owner().addWidgetChild(child.apply(scope))

        fun GuiScope<out IGWidget>.layer(layer: GuiLayer) {
            owner().layer = layer
        }

        fun GuiScope<out IGWidget>.active(active: Boolean) {
            owner().active = active
        }

        fun GuiScope<out IGWidget>.visible(visible: Boolean) {
            owner().visible = visible
        }

    }

}