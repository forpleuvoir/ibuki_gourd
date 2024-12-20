package moe.forpleuvoir.ibukigourd.gui.widget.tip

import kotlinx.coroutines.delay
import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.layout.AbsoluteLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.AbsoluteLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.tip.TipHelper
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl
import moe.forpleuvoir.ibukigourd.gui.screen.PopupScreen
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.Direction.Top
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Absolute
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxWidget
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.NotifiableArrayList
import moe.forpleuvoir.nebula.common.util.collection.notification
import moe.forpleuvoir.nebula.common.util.primitive.pick


class TipContainerWidget : WidgetContainerImpl(), AbsoluteLayout {

    private val log = logger()

    fun interface Scope : GuiScope<TipContainerWidget>, AbsoluteLayoutScope {

        fun Modifier.tipParent(widget: IGWidget) = this then WidgetModifier {
            it.parentData = WrappedTipData(widget)
        }

    }

    override fun <W : IGWidget> addWidgetChild(child: W): W {
        if (widgetChildren().size > 1000) return child
        return super.addWidgetChild(child)
    }

    /**
     * 清除已经 失效的Tip
     */
    private fun clearInvalidTips() {
        //修不了 干脆不修了
        screen()?.let { screen ->
            screen.launch {
                delay(500)
                runCatching {
                    val list = screen.flat()
                    val tips = widgetChildren()
                    val removeList = tips.filter {
                        WrappedTipData.fromTip(it)!!.parent !in list
                    }
                    removeList.forEach {
                        removeWidgetChild(it)
                    }
                }.onFailure {
                    log.error(it)
                }
            }

        }
    }
}

data class WrappedTipData(val parent: IGWidget) {
    companion object {
        fun fromTip(tip: IGWidget) = tip.parentData as? WrappedTipData
    }
}

typealias TipContainerScope = TipContainerWidget.Scope

private fun IGScreen.TipContainer(content: TipContainerScope.() -> Unit): TipContainerWidget {
    return (this.widgetChildren().find {
        it is TipContainerWidget
    }?.let {
        it as TipContainerWidget
    } ?: addWidgetChild(TipContainerWidget())).apply {
        layer = GuiLayer.Pop
        TipContainerScope { this }.content()
    }
}

fun WidgetScope.Tip(
    showState: State<Boolean>,
    parentTransform: () -> Transform = { owner().transform },
    modifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Colors.WHITE),
    optionalDirection: NotifiableArrayList<Direction> = Direction.entries.notification(),
    screen: IGScreen = mc.currentScreen as IGScreen,
    content: BoxScope.() -> Unit,
): BoxWidget {
    val parentWidget = owner()
    var box: BoxWidget? = null
    screen.TipContainer {
        val direction = mutableStateOf(optionalDirection.isNotEmpty().pick(optionalDirection.first(), Top))

        optionalDirection.subscribe {
            if (it.isEmpty()) {
                direction.setValue(Top)
            } else if (direction.getValue() !in it) {
                direction.setValue(it.first())
            }
        }
        box = Box(
            Modifier
                .tipParent(parentWidget)
                .active(showState)
                .visible(showState)
                .margin(4f)
                .padding(4f)
                .renderBackground { context, _, _, _ ->
                    TipHelper.updatePosition(this.transform, this.margin, direction, parentTransform(), optionalDirection)
                }
                .render { context, _, _, _ ->
                    TipHelper.tipRender(this.transform, context, direction.getValue(), parentTransform(), bgColor.getValue())
                }
                .placeCompletion {
                    if (transform.parent() != parentTransform()) transform.parent = parentTransform
                } then modifier
        ) {
            content()
        }
    }
    return box!!
}

fun WidgetScope.PopupTip(
    parentTransform: () -> Transform = { owner().transform },
    screenModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Colors.WHITE),
    optionalDirection: NotifiableArrayList<Direction> = Direction.entries.notification(),
    screen: IGScreen = mc.currentScreen as IGScreen,
    content: BoxScope.() -> Unit,
): IGScreenImpl = PopupScreen(screenModifier, screen) {
    val direction = mutableStateOf(optionalDirection.isNotEmpty().pick(optionalDirection.first(), Top))
    optionalDirection.subscribe {
        if (it.isEmpty()) {
            direction.setValue(Top)
        } else if (direction.getValue() !in it) {
            direction.setValue(it.first())
        }
    }
    Absolute {
        Box(
            Modifier
                .name("PopupTip")
                .mousePress {
                    onMousePress(it)
                    it.tryUse(!wasMouseOver).onSuccess {
                        screen()?.close()
                    }
                }
                .margin(4f)
                .padding(4f)
                .renderBackground { context, _, _, _ ->
                    TipHelper.updatePosition(this.transform, this.margin, direction, parentTransform(), optionalDirection)
                }
                .render { context, _, _, _ ->
                    TipHelper.tipRender(this.transform, context, direction.getValue(), parentTransform(), bgColor.getValue())
                }
                .placeCompletion {
                    if (transform.parent() != parentTransform()) transform.parent = parentTransform
                } then modifier,
            content
        )
    }
}


