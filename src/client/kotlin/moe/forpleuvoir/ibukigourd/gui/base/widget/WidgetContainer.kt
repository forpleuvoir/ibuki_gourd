package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable

interface WidgetContainer : Measurable {

    fun hoveredWidget(layer: GuiLayer): IGWidget? {
        for (child in widgetChildren()) {
            if (!child.active) continue
            if (child is WidgetContainer) {
                child.hoveredWidget(layer)?.let {
                    return if (it.active) it else null
                }
            }
            if (child.wasMouseOver && child.layer == layer) {
                return child
            }
        }
        return null
    }

    fun widgetChildren(): List<IGWidget>

    fun clearWidgetChildren()

    fun <W : IGWidget> addWidgetChild(child: W): W

    fun <W : IGWidget> addWidgetChild(child: W, scope: W.() -> Unit): W = addWidgetChild(child.apply(scope))

    fun <W : IGWidget> setWidgetChildren(index: Int, child: W): W

    fun removeWidgetChild(child: IGWidget): Boolean

    fun removeWidgetChildAt(index: Int): IGWidget?

}