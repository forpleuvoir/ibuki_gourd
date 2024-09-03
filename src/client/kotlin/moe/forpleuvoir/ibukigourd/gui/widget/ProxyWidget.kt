package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.stateOf

fun <T : WidgetContainerScope> T.Proxy(
    proxyState: State<T.() -> IGWidget>
): State<IGWidget> {
    val currentWidget = stateOf(proxyState.getValue().invoke(this))
    proxyState.subscribe { proxy ->
        val index = owner().widgetChildren().indexOf(currentWidget.getValue())
        val new = proxy.invoke(this)
        val widget = owner().setWidgetChildren(index, new)
        owner().removeWidgetChildAt(owner().widgetChildren().lastIndex)
        currentWidget.setValue(new)
        widget.screen()?.remeasure()
    }
    return currentWidget
}