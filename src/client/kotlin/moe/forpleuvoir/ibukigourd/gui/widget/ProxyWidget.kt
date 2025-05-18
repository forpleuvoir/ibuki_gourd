package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.execute
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf

fun <T : ContainerScope> T.Proxy(
    proxyState: State<T.() -> IGWidget>
): MutableState<IGWidget> {
    val currentWidget = mutableStateOf(proxyState.getValue().invoke(this))
    proxyState.subscribe { proxy ->
        runCatching {
            currentWidget.getValue().execute {
                val index = owner().widgetChildren().indexOf(currentWidget.getValue())
                val new = proxy.invoke(this)
                val widget = owner().setWidgetChildren(index, new)
                owner().removeWidgetChildAt(owner().widgetChildren().lastIndex)
                currentWidget.setValue(new)
                widget.remeasure()
            }
        }.onFailure {
            it.printStackTrace()
        }
    }
    return currentWidget
}

fun <T : ContainerScope> T.SwitchableProxy(
    widgetA: T.() -> IGWidget,
    widgetB: T.() -> IGWidget,
    switch: State<Boolean>,
): MutableState<IGWidget> {
    var state = switch.getValue()
    val editorProxy = mutableStateOf<T.() -> IGWidget> {
        if (state) widgetA()
        else widgetB()
    }
    switch.subscribe {
        state = it
        if (state) editorProxy.setValue(widgetA)
        else editorProxy.setValue(widgetB)
    }
    return Proxy(editorProxy)
}