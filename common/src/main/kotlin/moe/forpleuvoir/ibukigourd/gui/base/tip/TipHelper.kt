package moe.forpleuvoir.ibukigourd.gui.base.tip

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.util.math.component1
import moe.forpleuvoir.ibukigourd.util.math.component2
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.nebula.common.color.ARGBColor
import org.joml.Vector2f
import org.joml.Vector2fc

object TipHelper {

    /**
     * 更新给定 `Transform` 的位置，基于当前的变换、边距、方向以及父变换等信息。
     *
     * @param transform 当前需要更新位置的变换对象。
     * @param margin 边距信息，影响位置计算。
     * @param direction 表示方向的可变状态，用于更新位置时的方向判断。
     * @param parentTransform 父变换对象的提供函数，用于计算相对位置。
     * @param optionalDirection 可迭代的方向列表，用于提供可能的方向选项。
     */
    fun updatePosition(
        transform: Transform,
        margin: Margin,
        direction: MutableState<Direction>,
        parentTransform: Transform,
        optionalDirection: Iterable<Direction>
    ) {
        direction.setValue(checkDirection(transform, margin, parentTransform, optionalDirection))
        calcPosition(transform, margin, parentTransform, direction.getValue())
            .let { (x, y) -> transform.translateTo(x, y, false) }
        val x = transform.worldX.coerceIn(0f, (mc.window.guiScaledWidth.toFloat() - transform.width).coerceAtLeast(0f))
        val y = transform.worldY.coerceIn(0f, (mc.window.guiScaledHeight.toFloat() - transform.height).coerceAtLeast(0f))
        transform.translateTo(x, y, true)
    }

    /**
     * 渲染提示箭头及背景的方法。
     *
     * @param transform 当前提示框的变换信息。
     * @param context 绘图上下文，用于渲染内容。
     * @param direction 箭头方向，表示提示箭头指向的位置。
     * @param parentTransform 父级变换信息，用于计算箭头的位置和尺寸。
     * @param bgColor 当前状态的背景颜色。
     */
    fun tipRender(
        transform: Transform,
        guiGraphics: IGGuiGraphics,
        direction: Direction,
        parentTransform: Transform,
        bgColor: ARGBColor
    ) {
        //计算箭头位置
        val (pos, texture) = when (direction) {
            Direction.Top    -> Vector2f(
                parentTransform.worldCenter.x() - WidgetTextures.TIP_ARROW_TOP.halfWidth,
                transform.worldBottom
            ) to WidgetTextures.TIP_ARROW_TOP

            Direction.Bottom -> Vector2f(
                parentTransform.worldCenter.x() - WidgetTextures.TIP_ARROW_BOTTOM.halfWidth,
                transform.worldTop - WidgetTextures.TIP_ARROW_BOTTOM.height
            ) to WidgetTextures.TIP_ARROW_BOTTOM

            Direction.Left   -> Vector2f(
                transform.worldRight,
                parentTransform.worldCenter.y() - WidgetTextures.TIP_ARROW_LEFT.halfHeight
            ) to WidgetTextures.TIP_ARROW_LEFT

            Direction.Right  -> Vector2f(
                transform.worldLeft - WidgetTextures.TIP_ARROW_RIGHT.width,
                parentTransform.worldCenter.y() - WidgetTextures.TIP_ARROW_RIGHT.halfHeight
            ) to WidgetTextures.TIP_ARROW_RIGHT

        }
        transform.worldX = transform.worldX.toInt().toFloat()
        transform.worldY = transform.worldY.toInt().toFloat()
        guiGraphics {
            pos.x = pos.x.toInt().toFloat()
            pos.y = pos.y.toInt().toFloat()
            pushSpeechBubbleTexture(
                transform.asWorldCoordinateBox,
                WidgetTextures.TIP,
                Box(pos, Size(texture.width, texture.height).toFloat()),
                texture,
                direction.opposite(),
                bgColor,
                scissorBox = null
            )
        }
    }

    private fun calcPosition(ref: Size<Float>, margin: Margin, parent: Transform, direction: Direction): Vector2fc {
        val pos = when (direction) {
            Direction.Top    -> Vector2f(parent.halfWidth - ref.halfWidth, -margin.bottom - ref.height)
            Direction.Right  -> Vector2f(parent.width + margin.left, parent.halfHeight - ref.halfHeight)
            Direction.Bottom -> Vector2f(parent.halfWidth - ref.halfWidth, parent.height + margin.top)
            Direction.Left   -> Vector2f(-margin.right - ref.width, parent.halfHeight - ref.halfHeight)
        }
        return pos
    }

    private fun checkDirection(ref: Size<Float>, margin: Margin, parent: Transform, optionalDirection: Iterable<Direction>): Direction {
        //------------ 计算如果没有可放置位置则选择一个空间最大的方向放置 ------------\\
        val leftSpace = Direction.Left to (parent.worldLeft)
        val rightSpace = Direction.Right to (mc.window.guiScaledWidth - parent.worldRight)
        val topSpace = Direction.Top to (parent.worldTop)
        val bottomSpace = Direction.Bottom to (mc.window.guiScaledHeight - parent.worldBottom)
        return optionalDirection.find {
            when (it) {
                Direction.Left   -> leftSpace.second >= ref.width + margin.right
                Direction.Right  -> rightSpace.second >= ref.width + margin.left
                Direction.Top    -> topSpace.second >= ref.height + margin.bottom
                Direction.Bottom -> bottomSpace.second >= ref.height + margin.top
            }
        } ?: arrayOf(leftSpace, rightSpace, topSpace, bottomSpace).maxBy { if (it.first in optionalDirection) it.second else -114514f }.first
    }

}