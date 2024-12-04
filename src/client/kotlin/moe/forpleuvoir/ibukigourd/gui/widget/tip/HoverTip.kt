package moe.forpleuvoir.ibukigourd.gui.widget.tip

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxWidget
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.NotifiableArrayList
import moe.forpleuvoir.nebula.common.util.collection.notification
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

fun WidgetScope.HoverTip(
    showDelay: Duration = 200.milliseconds,
    closeDelay: Duration = 0.seconds,
    keepShow: MutableState<Boolean> = mutableStateOf(false),
    parentTransform: () -> Transform = { owner().transform },
    modifier: Modifier = Modifier,
    bgColor: MutableState<ARGBColor> = mutableStateOf(Colors.WHITE),
    optionalDirection: NotifiableArrayList<Direction> = Direction.entries.notification(),
    screen: IGScreen = mc.currentScreen as IGScreen,
    content: BoxScope.() -> Unit,
): BoxWidget {
    var tip: BoxWidget? = null
    val parentWidget = owner()
    val showState = mutableStateOf(false)
    var currentJob: Job? = null
    var hoverState = false
    keepShow.subscribe {
        if (!it && !(screen.hoveredWidget.getValue()?.hasParentInChain(parentWidget) == true && parentWidget.wasMouseOver)) {
            showState.setValue(false)
            hoverState = false
            currentJob?.cancel()
        }
    }
    screen.hoveredWidget.subscribe {
        //如果悬浮组件为当前tip
        if (it?.hasParentInChain(tip!!) == true) {
            return@subscribe
        }
        //如果已经为显示状态且保持显示则不更新
        if (showState.getValue() && keepShow.getValue()) return@subscribe
        //更新悬浮状态
        val oldState = hoverState
        hoverState = it?.hasParentInChain(parentWidget) == true && parentWidget.wasMouseOver
        //状态更新时
        if (hoverState != oldState) {
            //取消之前的任务
            currentJob?.cancel()
            //当前悬浮状态为False,触发关闭任务
            if (hoverState) {
                if (showDelay == Duration.ZERO) showState.setValue(true)
                currentJob = screen.launch {
                    delay(showDelay)
                    showState.setValue(hoverState)
                }
            } else {
                currentJob = screen.launch {
                    if (closeDelay == Duration.ZERO) showState.setValue(false)
                    delay(closeDelay)
                    showState.setValue(hoverState)
                }
            }
        }
    }
    tip = Tip(showState, parentTransform, modifier, bgColor, optionalDirection, screen, content)
    return tip
}

