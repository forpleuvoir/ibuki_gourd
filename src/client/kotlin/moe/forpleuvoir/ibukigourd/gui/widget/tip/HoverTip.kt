package moe.forpleuvoir.ibukigourd.gui.widget.tip

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.batchRenderTextureColored
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.widget.*
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.render.texture.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetScope
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.Direction.*
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Absolute
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxWidget
import moe.forpleuvoir.ibukigourd.render.math.component1
import moe.forpleuvoir.ibukigourd.render.math.component2
import moe.forpleuvoir.ibukigourd.util.State
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.stateOf
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.collection.NotifiableArrayList
import moe.forpleuvoir.nebula.common.util.collection.notification
import moe.forpleuvoir.nebula.common.util.primitive.pick
import org.joml.Vector2f
import org.joml.Vector2fc
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private typealias BoxShape = Box

fun WidgetScope.HoverTip(
    showDelay: Duration = 200.milliseconds,
    closeDelay: Duration = 0.seconds,
    keepShow: State<Boolean> = stateOf(false),
    parentTransform: (IGWidget) -> Transform = { it.transform },
    modifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Colors.WHITE),
    optionalDirection: NotifiableArrayList<Direction> = Direction.entries.notification(),
    screen: IGScreen = mc.currentScreen as IGScreen,
    content: BoxScope.() -> Unit,
): BoxWidget {
    var tip: BoxWidget? = null
    val parentWidget = owner()
    val showState = stateOf(false)
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

fun WidgetScope.Tip(
    showState: State<Boolean>,
    parentTransform: (IGWidget) -> Transform = { it.transform },
    modifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Colors.WHITE),
    optionalDirection: NotifiableArrayList<Direction> = Direction.entries.notification(),
    screen: IGScreen = mc.currentScreen as IGScreen,
    content: BoxScope.() -> Unit,
): BoxWidget {
    val parentWidget = owner()
    var box: BoxWidget? = null
    screen.scope.Absolute {
        val direction = stateOf(optionalDirection.isNotEmpty().pick(optionalDirection.first(), Top))

        optionalDirection.subscribe {
            if (it.isEmpty()) {
                direction.setValue(Top)
            } else if (direction.getValue() !in it) {
                direction.setValue(it.first())
            }
        }
        box = Box(
            Modifier
                .active(showState)
                .visible(showState)
                .margin(4f)
                .padding(4f)
                .layer(GuiLayer.Pop)
                .render { context, _, _, _ ->
                    direction.setValue(checkDirection(transform, margin, parentTransform(parentWidget), optionalDirection))
                    calcPosition(transform, margin, parentTransform(parentWidget), direction.getValue())
                        .let { (x, y) -> transform.translateTo(x, y, false) }
                    val x = transform.worldX.coerceIn(0f, mc.window.scaledWidth.toFloat() - transform.width)
                    val y = transform.worldY.coerceIn(0f, mc.window.scaledHeight.toFloat() - transform.height)
                    transform.translateTo(x, y, true)
                    //计算箭头位置
                    val (pos, texture) = when (direction.getValue()) {
                        Top    -> Vector2f(
                            parentTransform(parentWidget).worldCenter.x() - WidgetTextures.TIP_ARROW_TOP.halfWidth,
                            transform.worldBottom
                        ) to WidgetTextures.TIP_ARROW_TOP

                        Right  -> Vector2f(
                            transform.worldLeft - WidgetTextures.TIP_ARROW_RIGHT.width,
                            parentTransform(parentWidget).worldCenter.y() - WidgetTextures.TIP_ARROW_RIGHT.halfHeight
                        ) to WidgetTextures.TIP_ARROW_RIGHT

                        Bottom -> Vector2f(
                            parentTransform(parentWidget).worldCenter.x() - WidgetTextures.TIP_ARROW_BOTTOM.halfWidth,
                            transform.worldTop - WidgetTextures.TIP_ARROW_BOTTOM.height
                        ) to WidgetTextures.TIP_ARROW_BOTTOM

                        Left   -> Vector2f(
                            transform.worldRight,
                            parentTransform(parentWidget).worldCenter.y() - WidgetTextures.TIP_ARROW_LEFT.halfHeight
                        ) to WidgetTextures.TIP_ARROW_LEFT
                    }
                    context.batchRenderTextureColored {
                        pushWidgetTexture(transform, WidgetTextures.TIP, color = bgColor.getValue())
                        pushWidgetTexture(BoxShape(pos, Size(texture.width, texture.height).toFloat()), texture, color = bgColor.getValue())
                    }
                }
                .placeCompletion {
                    if (transform.parent() != parentWidget.transform) transform.parent = { parentWidget.transform }
                } then modifier
        ) {
            content()
        }
    }
    return box!!
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