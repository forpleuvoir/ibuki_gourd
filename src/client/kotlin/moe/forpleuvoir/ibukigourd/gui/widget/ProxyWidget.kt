package moe.forpleuvoir.ibukigourd.gui.widget

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.task.scheduleEndTick
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.stateOf
import kotlin.time.Duration


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

fun <T : WidgetContainerScope> T.Proxy(
    proxyState: State<T.() -> IGWidget>,
    delay: Int
): State<IGWidget> {
    val currentWidget = stateOf(proxyState.getValue().invoke(this))
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
): State<IGWidget> {
    val currentWidget = stateOf(proxyState.getValue().invoke(this))
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