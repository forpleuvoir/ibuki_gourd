package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable

interface WidgetContainer : Measurable {

    fun hoveredWidget(): IGWidget? {
        widgetChildren().forEach { widget ->
            if (widget is WidgetContainer && widget.wasMouseOver) {
                widget.hoveredWidget()?.let {
                    return it
                }
                return widget
            }
            if (widget.wasMouseOver) {
                return widget
            }
        }
        return null
    }

    fun widgetChildren(): List<IGWidget>

    fun clearWidgetChildren()

    fun <W : IGWidget> addWidgetChild(child: W): W

    fun <W : IGWidget> addWidgetChild(child: W, scope: W.() -> Unit): W = addWidgetChild(child.apply(scope))

}