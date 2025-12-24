package moe.forpleuvoir.ibukigourd.gui.base.tip

import moe.forpleuvoir.ibukigourd.gui.base.Margin
import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.util.Direction
import moe.forpleuvoir.ibukigourd.gui.util.Direction.Bottom
import moe.forpleuvoir.ibukigourd.gui.util.Direction.Left
import moe.forpleuvoir.ibukigourd.gui.util.Direction.Right
import moe.forpleuvoir.ibukigourd.gui.util.Direction.Top
import moe.forpleuvoir.ibukigourd.util.math.component1
import moe.forpleuvoir.ibukigourd.util.math.component2
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.common.color.ARGBColor
import org.joml.Vector2f
import org.joml.Vector2fc

object TipHelper {

    private fun spaceByDirection(parent: Box, margin: Margin, direction: Direction): SizeFloat {
        return when (direction) {
            Top    -> Size(mc.window.guiScaledWidth.toFloat(), parent.top - margin.bottom)
            Bottom -> Size(mc.window.guiScaledWidth.toFloat(), mc.window.guiScaledHeight.toFloat() - parent.bottom - margin.top)
            Left   -> Size(parent.right - margin.left, mc.window.guiScaledHeight.toFloat())
            Right  -> Size(mc.window.guiScaledWidth.toFloat() - parent.left - margin.right, mc.window.guiScaledHeight.toFloat())
        }
    }

    private fun Size<Float>.calculateScore(baseSize: Size<Float>? = null): Float {
        val isWidthAtMaximum = baseSize != null && width >= baseSize.width
        val isHeightAtMaximum = baseSize != null && height >= baseSize.height

        if (isWidthAtMaximum && isHeightAtMaximum) {
            return 114514f
        }

        // 宽度得分 1:1 比例
        val widthScore = if (isWidthAtMaximum) baseSize.width else width
        // 高度得分 1:1.7777778 比例（约等于 16:9 的比例系数）
        val heightScore = (if (isHeightAtMaximum) baseSize.height else height) * 1.7777778f

        // 如果宽高低于某个阈值，则对整体得分打折
        val minWidthThreshold = 32f
        val minHeightThreshold = 18f

        val widthFactor = if (width < minWidthThreshold) width / minWidthThreshold else 1.0f
        val heightFactor = if (height < minHeightThreshold) height / minHeightThreshold else 1.0f

        // 应用折扣因子
        return widthScore * if (isWidthAtMaximum) 1f else widthFactor +
                heightScore * if (isHeightAtMaximum) 1f else heightFactor
    }

    fun evaluatePlacementOptions(baseSize: Size<Float>?, parent: Box, margin: Margin, optionalDirection: List<Direction>): Pair<SizeFloat, Direction> {
        // 如果只有一个可选方向，直接使用该方向的约束
        if (optionalDirection.size == 1) {
            return spaceByDirection(parent, margin, optionalDirection.first()) to optionalDirection.first()
        }
        // 根据可选方向的顺序优先级选择分数最高的方向
        val suitableDirection = optionalDirection.maxByOrNull {
            spaceByDirection(parent, margin, it).calculateScore(baseSize)
        }!!
        return spaceByDirection(parent, margin, suitableDirection) to suitableDirection
    }

    /**
     * 更新元素的位置，根据指定方向和约束条件计算新的坐标位置
     *
     * @param transform 元素的变换对象，包含位置和尺寸信息
     * @param margin 元素的边距设置
     * @param parent 父容器的变换对象
     * @param optionalDirection 可选的方向集合
     * @return 返回更新后的方向对象
     */
    fun updatePosition(
        transform: Transform,
        margin: Margin,
        parent: Box,
        direction: Direction,
    ) {
        when (direction) {
            Top    -> Vector2f(parent.centerX - transform.halfWidth, parent.top - margin.bottom - transform.height)
            Right  -> Vector2f(parent.right + margin.left, parent.centerY - transform.halfHeight)
            Bottom -> Vector2f(parent.centerX - transform.halfWidth, parent.bottom + margin.top)
            Left   -> Vector2f(parent.left - margin.right - transform.width, parent.centerY - transform.halfHeight)
        }.let { (x, y) ->
            transform.translateTo(
                x.coerceIn(0f, (mc.window.guiScaledWidth.toFloat() - transform.width).coerceAtLeast(0f)),
                y.coerceIn(0f, (mc.window.guiScaledHeight.toFloat() - transform.height).coerceAtLeast(0f)),
                true
            )
        }
    }

    /**
     * 渲染提示箭头及背景的方法。
     *
     * @param transform 当前提示框的变换信息。
     * @param context 绘图上下文，用于渲染内容。
     * @param direction 箭头方向，表示提示箭头指向的位置。
     * @param parentBox 父级变换信息，用于计算箭头的位置和尺寸。
     * @param bgColor 当前状态的背景颜色。
     */
    fun tipRender(
        transform: Transform,
        guiGraphics: IGGuiGraphics,
        direction: Direction,
        parentBox: Box,
        bgColor: ARGBColor
    ) {
        //修正气泡位置 由于浮点位置小数点部分可能无法被 正常渲染,只能强制使用Int
        transform.worldX = transform.worldX.toInt().toFloat()
        transform.worldY = transform.worldY.toInt().toFloat()
        //计算箭头位置
        val (pos, texture) = when (direction) {
            Top    -> Vector2f(
                parentBox.centerX - WidgetTextures.TIP_ARROW_TOP.halfWidth,
                transform.worldBottom
            ) to WidgetTextures.TIP_ARROW_TOP

            Bottom -> Vector2f(
                parentBox.centerX - WidgetTextures.TIP_ARROW_BOTTOM.halfWidth,
                transform.worldTop - WidgetTextures.TIP_ARROW_BOTTOM.height
            ) to WidgetTextures.TIP_ARROW_BOTTOM

            Left   -> Vector2f(
                transform.worldRight,
                parentBox.centerY - WidgetTextures.TIP_ARROW_LEFT.halfHeight
            ) to WidgetTextures.TIP_ARROW_LEFT

            Right  -> Vector2f(
                transform.worldLeft - WidgetTextures.TIP_ARROW_RIGHT.width,
                parentBox.centerY - WidgetTextures.TIP_ARROW_RIGHT.halfHeight
            ) to WidgetTextures.TIP_ARROW_RIGHT

        }
        guiGraphics {
            //修正箭头位置 由于浮点位置小数点部分可能无法被 正常渲染,只能强制使用Int
//            pos.x = pos.x.toInt().toFloat()
//            pos.y = pos.y.toInt().toFloat()
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

    /**
     * 检查元素在指定方向上是否可以放置
     *
     * @param ref 要放置的元素尺寸信息，包含宽度和高度
     * @param margin 元素的边距信息
     * @param parent 父容器的边界信息
     * @param optionalDirection 可选的放置方向列表
     * @return 返回可以放置元素的方向列表，如果没有任何方向可以放置则返回空列表
     */
    fun canPlaceDirections(ref: Size<Float>, margin: Margin, parent: Box, optionalDirection: List<Direction>): List<Direction> {
        return optionalDirection.filter {
            when (it) {
                Top    -> parent.top + margin.bottom >= ref.height
                Right  -> mc.window.guiScaledWidth - parent.right - margin.left >= ref.width
                Bottom -> mc.window.guiScaledHeight - parent.bottom - margin.top >= ref.height
                Left   -> parent.left + margin.right >= ref.width
            }
        }
    }

}