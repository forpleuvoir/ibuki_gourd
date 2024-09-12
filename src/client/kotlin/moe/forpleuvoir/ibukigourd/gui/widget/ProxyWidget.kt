package moe.forpleuvoir.ibukigourd.gui.widget

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.task.scheduleEndTick
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import kotlin.time.Duration


fun <T : WidgetContainerScope> T.Proxy(
    proxyState: State<T.() -> IGWidget>
): MutableState<IGWidget> {
    val currentWidget = mutableStateOf(proxyState.getValue().invoke(this))
    proxyState.subscribe { proxy ->
        kotlin.runCatching {
            val index = owner().widgetChildren().indexOf(currentWidget.getValue())
            val new = proxy.invoke(this)
            val widget = owner().setWidgetChildren(index, new)
            owner().removeWidgetChildAt(owner().widgetChildren().lastIndex)
            currentWidget.setValue(new)
            widget.screen()?.remeasure()
        }.onFailure {
            it.printStackTrace()
        }
    }
    return currentWidget
}

fun <T : WidgetContainerScope> T.Proxy(
    proxyState: State<T.() -> IGWidget>,
    delay: Int
): MutableState<IGWidget> {
    val currentWidget = mutableStateOf(proxyState.getValue().invoke(this))
    proxyState.subscribe { proxy ->
        mc.scheduleEndTick(delay) {
            val index = owner().widgetChildren().indexOf(currentWidget.getValue())
            val new = proxy.invoke(this)
            val widget = owner().setWidgetChildren(index, new)
            owner().removeWidgetChildAt(owner().widgetChildren().lastIndex)
            currentWidget.setValue(new)
            widget.screen()?.remeasure()
        }
    }
    return currentWidget
}

fun <T : WidgetContainerScope> T.Proxy(
    proxyState: State<T.() -> IGWidget>,
    delay: Duration
): MutableState<IGWidget> {
    val currentWidget = mutableStateOf(proxyState.getValue().invoke(this))
    var currentJob: Job? = null
    proxyState.subscribe { proxy ->
        currentJob?.cancel()
        currentJob = currentWidget.getValue().screen()!!.launch {
            delay(delay)
            val index = owner().widgetChildren().indexOf(currentWidget.getValue())
            val new = proxy.invoke(this@Proxy)
            val widget = owner().setWidgetChildren(index, new)
            owner().removeWidgetChildAt(owner().widgetChildren().lastIndex)
            currentWidget.setValue(new)
            widget.screen()?.remeasure()
        }
    }
    return currentWidget
}