package moe.forpleuvoir.ibukigourd.gui.widget.tip

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
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.Direction.*
import moe.forpleuvoir.ibukigourd.gui.util.renderHoveredOutlineBox
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Absolute
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Box
import moe.forpleuvoir.ibukigourd.gui.widget.layout.BoxScope
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

private typealias BoxShape = Box

fun WidgetScope.HoverTip(
    modifier: Modifier = Modifier,
    bgColor: State<ARGBColor> = stateOf(Colors.WHITE),
    optionalDirection: NotifiableArrayList<Direction> = Direction.entries.notification(),
    content: BoxScope.() -> Unit,
) {
    val parent = owner()
    val screen = (mc.currentScreen as? IGScreen)
    screen?.scope?.run {
        Absolute {
            val direction: State<Direction> = stateOf(optionalDirection.isNotEmpty().pick(optionalDirection.first, Top))

            val active = stateOf(false)

            screen.hoveredWidget.subscribe {
                active.setValue(it?.hasParentInChain(parent) ?: false && parent.wasMouseOver)
            }
            optionalDirection.subscribe {
                if (it.isEmpty()) {
                    direction.setValue(Top)
                } else if (direction.getValue() !in it) {
                    direction.setValue(it.first())
                }
            }
            Box(
                modifier
                    .active(active)
                    .visible(active)
                    .margin(4f)
                    .padding(4f)
                    .layer(GuiLayer.Pop)
                    .renderHoveredOutlineBox(Colors.AQUA)
                    .render { context, _, _, _ ->
                        val x = transform.worldX.coerceIn(0f, mc.window.scaledWidth.toFloat() - transform.width)
                        val y = transform.worldY.coerceIn(0f, mc.window.scaledHeight.toFloat() - transform.height)
                        transform.translateTo(x, y, true)
                        //计算箭头位置
                        val (pos, texture) = when (direction.getValue()) {
                            Top    -> Vector2f(
                                parent.transform.worldCenter.x() - WidgetTextures.TIP_ARROW_TOP.halfWidth,
                                transform.worldBottom - 2
                            ) to WidgetTextures.TIP_ARROW_TOP

                            Right  -> Vector2f(
                                transform.worldLeft + 2 - WidgetTextures.TIP_ARROW_BOTTOM.width,
                                parent.transform.worldCenter.y() - WidgetTextures.TIP_ARROW_RIGHT.halfHeight
                            ) to WidgetTextures.TIP_ARROW_RIGHT

                            Bottom -> Vector2f(
                                parent.transform.worldCenter.x() - WidgetTextures.TIP_ARROW_BOTTOM.halfWidth,
                                transform.worldTop + 2 - WidgetTextures.TIP_ARROW_BOTTOM.height
                            ) to WidgetTextures.TIP_ARROW_BOTTOM

                            Left   -> Vector2f(
                                transform.worldRight - 2,
                                parent.transform.worldCenter.y() - WidgetTextures.TIP_ARROW_LEFT.halfHeight
                            ) to WidgetTextures.TIP_ARROW_LEFT
                        }
                        context.batchRenderTextureColored {
                            pushWidgetTexture(transform, WidgetTextures.TIP, color = bgColor.getValue())
                            pushWidgetTexture(BoxShape(pos, Size(texture.width, texture.height).toFloat()), texture, color = bgColor.getValue())
                        }
                    }
                    .placeCompleted {
                        if (transform.parent() != parent.transform) transform.parent = { parent.transform }
                        direction.setValue(checkDirection(transform, margin, parent.transform, optionalDirection))
                        val pos = calcPosition(transform, margin, parent.transform, direction.getValue())
                        transform.translateTo(pos.x(), pos.y(), false)
                    }
            ) {
                content()
            }
        }


    }
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