package moe.forpleuvoir.ibukigourd.gui.widget.tip

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderBox
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreenImpl
import moe.forpleuvoir.ibukigourd.gui.base.screen.bgBlurRadius
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.screen.AbsoluteScreen
import moe.forpleuvoir.ibukigourd.gui.screen.AbsoluteScreenScope
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.Direction.Top
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxWidget
import moe.forpleuvoir.ibukigourd.mod.gui.GuiConfig.PopupScreen.DEFAULT_BG_BLUR_RADIUS
import moe.forpleuvoir.ibukigourd.render.renderBlur
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.openScreen
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.NotifiableArrayList
import moe.forpleuvoir.nebula.common.util.collection.notification
import moe.forpleuvoir.nebula.common.util.primitive.pick
import net.minecraft.client.gui.screen.Screen

fun WidgetScope.PopupTip(
    showState: MutableState<Boolean>,
    parentTransform: (IGWidget) -> Transform = { it.transform },
    screenModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Colors.WHITE),
    optionalDirection: NotifiableArrayList<Direction> = Direction.entries.notification(),
    screen: IGScreen = mc.currentScreen as IGScreen,
    content: BoxScope.() -> Unit,
) = PopupScreen(showState, screenModifier, screen) {
    val parentWidget = this@PopupTip.owner()
    var box: BoxWidget? = null
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
            .name("PopupTip")
            .mousePress {
                onMousePress(it)
                it.tryUse(!wasMouseOver).onSuccess {
                    showState.setValue(false)
                }
                it.tryUse()
            }
            .margin(4f)
            .padding(4f)
            .render(tipRender(direction, parentTransform, parentWidget, optionalDirection, bgColor))
            .placeCompletion {
                if (transform.parent() != parentWidget.transform) transform.parent = { parentWidget.transform }
            } then modifier
    ) {
        content()
    }
}

fun PopupScreen(
    showState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    parentScreen: IGScreen = mc.currentScreen as IGScreen,
    content: AbsoluteScreenScope.() -> Unit,
) {
    var screen: IGScreenImpl<*>? = null
    val popupScreen = {
        screen = AbsoluteScreen(
            Modifier
                .name("PopupScreen")
                .bgBlurRadius(DEFAULT_BG_BLUR_RADIUS)
                .renderBackground { context, x, y, d ->
                    screen()?.parentScreen?.render(context, 0, 0, d)

                    mc.gameRenderer.renderBlur((this as IGScreen).bgBlurRadius, d)
                    mc.framebuffer.beginWrite(false)

                    context.batchRenderBox {
                        pushBoxOutline(transform, Colors.AQUA)
                    }
                }
                .mouseRelease {
                    screen()?.parentScreen?.mouseReleased(it.x.toDouble(), it.y.toDouble(), it.button.code)
                    onMouseRelease(it)
                }
                .keyRelease {
                    screen()?.parentScreen?.keyReleased(it.keyCode.code, it.scanCode, it.modifiers)
                    onKeyRelease(it)
                }
                .then(modifier)
        ) {
            owner().parentScreen = parentScreen as Screen
            owner().screen()?.let { s ->
                s.onResize = { client, width, height ->
                    s.parentScreen?.resize(client, width, height)
                }
                s.onFirstInit = { client, width, height ->
                    s.parentScreen?.init(client, width, height)
                }
                s.onInit = {
                    s.parentScreen?.init()
                }
            }
            owner().onClose = {
                showState.setValue(false)
            }
            //------------ Content ------------\\
            content()
        }
        screen
    }

    showState.subscribe {
        if (it) {
            openScreen(popupScreen())
        } else {
            screen?.close()
        }
    }
}