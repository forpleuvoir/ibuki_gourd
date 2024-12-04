package moe.forpleuvoir.ibukigourd.gui.widget.tip

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.layout.AbsoluteLayout
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.scope.AbsoluteLayoutScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope.Companion.addWidgetChild
import moe.forpleuvoir.ibukigourd.gui.base.scope.ScreenScope
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetContainerImpl
import moe.forpleuvoir.ibukigourd.gui.screen.PopupScreen
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.Direction.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Absolute
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxWidget
import moe.forpleuvoir.ibukigourd.render.math.component1
import moe.forpleuvoir.ibukigourd.render.math.component2
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
import org.joml.Vector2f
import org.joml.Vector2fc


class TipContainerWidget : WidgetContainerImpl(), AbsoluteLayout {
    fun interface Scope : GuiScope<TipContainerWidget>, AbsoluteLayoutScope {

        fun Modifier.tipParent(widget: IGWidget) = this then WidgetModifier {
            it.parentData = WrappedTipData(widget)
        }

    }

    override fun <W : IGWidget> addWidgetChild(child: W): W {
        clearInvalidTips()
        return super.addWidgetChild(child)
    }

    /**
     * 清除已经 失效的Tip
     */
    private fun clearInvalidTips() {
        screen()?.let { screen ->
            val list = screen.flat()
            val removeList = widgetChildren().filter {
                WrappedTipData.fromTip(it)!!.parent in list
            }
            removeList.forEach {
                removeWidgetChild(it)
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

fun ScreenScope<*>.TipContainer(content: TipContainerScope.() -> Unit): TipContainerWidget {
    return (owner().widgetChildren().find {
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
    screen.scope.TipContainer {
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
                .renderBackground(updatePosition(direction, parentTransform, optionalDirection))
                .render(tipRender(direction, parentTransform, bgColor))
                .placeCompletion {
                    if (transform.parent() != parentTransform()) transform.parent = parentTransform
                } then modifier
        ) {
            content()
        }
    }
    return box!!
}

fun tipRender(
    direction: MutableState<Direction>,
    parentTransform: () -> Transform,
    bgColor: State<ARGBColor>
): IGWidget.(IGDrawContext, Float, Float, Float) -> Unit = { context, _, _, _ ->
    //计算箭头位置
    val (pos, texture) = when (direction.getValue()) {
        Top    -> Vector2f(
            parentTransform().worldCenter.x() - WidgetTextures.TIP_ARROW_TOP.halfWidth,
            transform.worldBottom
        ) to WidgetTextures.TIP_ARROW_TOP

        Right  -> Vector2f(
            transform.worldLeft - WidgetTextures.TIP_ARROW_RIGHT.width,
            parentTransform().worldCenter.y() - WidgetTextures.TIP_ARROW_RIGHT.halfHeight
        ) to WidgetTextures.TIP_ARROW_RIGHT

        Bottom -> Vector2f(
            parentTransform().worldCenter.x() - WidgetTextures.TIP_ARROW_BOTTOM.halfWidth,
            transform.worldTop - WidgetTextures.TIP_ARROW_BOTTOM.height
        ) to WidgetTextures.TIP_ARROW_BOTTOM

        Left   -> Vector2f(
            transform.worldRight,
            parentTransform().worldCenter.y() - WidgetTextures.TIP_ARROW_LEFT.halfHeight
        ) to WidgetTextures.TIP_ARROW_LEFT
    }
    context.batchRenderTextureColored {
        pushWidgetTexture(transform, WidgetTextures.TIP, color = bgColor.getValue())
        pushWidgetTexture(Box(pos, Size(texture.width, texture.height).toFloat()), texture, color = bgColor.getValue())
    }
}

fun WidgetScope.OpenPopupTip(
    parentTransform: () -> Transform = { owner().transform },
    screenModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Colors.WHITE),
    optionalDirection: NotifiableArrayList<Direction> = Direction.entries.notification(),
    screen: IGScreen = mc.currentScreen as IGScreen,
    content: BoxScope.() -> Unit,
) = openScreen(PopupTip(parentTransform, screenModifier, modifier, bgColor, optionalDirection, screen, content))

fun WidgetScope.PopupTip(
    parentTransform: () -> Transform = { owner().transform },
    screenModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Colors.WHITE),
    optionalDirection: NotifiableArrayList<Direction> = Direction.entries.notification(),
    screen: IGScreen = mc.currentScreen as IGScreen,
    content: BoxScope.() -> Unit,
) = PopupScreen(screenModifier, screen) {

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
                .renderBackground(updatePosition(direction, parentTransform, optionalDirection))
                .render(tipRender(direction, parentTransform, bgColor))
                .placeCompletion {
                    if (transform.parent() != parentTransform()) transform.parent = parentTransform
                } then modifier
        ) {
            content()
        }
    }
}

fun updatePosition(
    direction: MutableState<Direction>,
    parentTransform: () -> Transform,
    optionalDirection: NotifiableArrayList<Direction>
): IGWidget.(IGDrawContext, Float, Float, Float) -> Unit = { context, _, _, _ ->
    direction.setValue(checkDirection(transform, margin, parentTransform(), optionalDirection))
    calcPosition(transform, margin, parentTransform(), direction.getValue())
        .let { (x, y) -> transform.translateTo(x, y, false) }
    val x = transform.worldX.coerceIn(0f, (mc.window.scaledWidth.toFloat() - transform.width).coerceAtLeast(0f))
    val y = transform.worldY.coerceIn(0f, (mc.window.scaledHeight.toFloat() - transform.height).coerceAtLeast(0f))
    transform.translateTo(x, y, true)
}


private fun calcPosition(ref: Size<Float>, margin: Margin, parent: Transform, direction: Direction): Vector2fc {
    val pos = when (direction) {
        Top    -> Vector2f(parent.halfWidth - ref.halfWidth, -margin.bottom - ref.height)
        Right  -> Vector2f(parent.width + margin.left, parent.halfHeight - ref.halfHeight)
        Bottom -> Vector2f(parent.halfWidth - ref.halfWidth, parent.height + margin.top)
        Left   -> Vector2f(-margin.right - ref.width, parent.halfHeight - ref.halfHeight)
    }
    return pos
}

private fun checkDirection(ref: Size<Float>, margin: Margin, parent: Transform, optionalDirection: Iterable<Direction>): Direction {
    //------------ 计算如果没有可放置位置则选择一个空间最大的方向放置 ------------\\
    val leftSpace = Left to (parent.worldLeft)
    val rightSpace = Right to (mc.window.scaledWidth - parent.worldRight)
    val topSpace = Top to (parent.worldTop)
    val bottomSpace = Bottom to (mc.window.scaledHeight - parent.worldBottom)
    return optionalDirection.find {
        when (it) {
            Left   -> leftSpace.second >= ref.width + margin.right
            Right  -> rightSpace.second >= ref.width + margin.left
            Top    -> topSpace.second >= ref.height + margin.bottom
            Bottom -> bottomSpace.second >= ref.height + margin.top
        }
    } ?: arrayOf(leftSpace, rightSpace, topSpace, bottomSpace).maxBy { if (it.first in optionalDirection) it.second else -114514f }.first
}