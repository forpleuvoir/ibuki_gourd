@file:Suppress("DuplicatedCode", "unused")

package moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.ColoredBox
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.pointsInCircleRange
import moe.forpleuvoir.ibukigourd.render.color
import moe.forpleuvoir.ibukigourd.render.vertex
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.math.plus
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.common.color.alphaFRange
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import org.joml.Vector2fc
import kotlin.math.abs
import kotlin.math.min

fun DrawContext.batchRenderBox(
    layer: RenderLayer = RenderLayer.getGui(),
    block: BoxBatchRenderScope.() -> Unit
) {
    block(BoxBatchRenderScope(vertexConsumers.getBuffer(layer), this.matrices))
    draw()
}

fun batchRenderBox(
    vertexConsumers: VertexConsumerProvider.Immediate,
    matrices: MatrixStack,
    layer: RenderLayer = RenderLayer.getGui(),
    block: BoxBatchRenderScope.() -> Unit
) {
    block(BoxBatchRenderScope(vertexConsumers.getBuffer(layer), matrices))
    vertexConsumers.draw()
}

@ConsistentCopyVisibility
@Suppress("MemberVisibilityCanBePrivate")
data class BoxBatchRenderScope internal constructor(val bufferBuilder: VertexConsumer, val matrices: MatrixStack) {

    /**
     * 渲染一个[Box]
     * @param box Box
     * @param color ARGBColor
     */
    fun pushBox(box: Box, color: ARGBColor) {
        for (vertex in box.vertexes) {
            bufferBuilder.vertex(matrices, vertex).color(color)
        }
    }

    /**
     * 渲染一个[Box]
     * @param transform Transform
     * @param color ARGBColor
     */
    fun pushBox(transform: Transform, color: ARGBColor) =
        pushBox(transform.asWorldCoordinateBox, color)

    /**
     * 渲染一个[ColoredBox]
     * @param coloredBox ColoredBox
     */
    fun pushBox(coloredBox: ColoredBox) {
        for (vertex in coloredBox.coloredVertexes) {
            bufferBuilder.vertex(matrices, vertex).color(vertex.color)
        }
    }

    /**
     * 渲染一个[Box]
     * @param x Number
     * @param y Number
     * @param width Number
     * @param height Number
     * @param color ARGBColor
     */
    fun pushBox(x: Float, y: Float, width: Float, height: Float, color: ARGBColor) {
        bufferBuilder.vertex(matrices, x = x, y = y, 0f).color(color)
        bufferBuilder.vertex(matrices, x = x, y = y + height, 0f).color(color)
        bufferBuilder.vertex(matrices, x = x + width, y = y + height, 0f).color(color)
        bufferBuilder.vertex(matrices, x = x + width, y = y, 0f).color(color)
    }

    /**
     * 渲染一个[Box]
     * @param position Vector2fc
     * @param size Size<Float>
     * @param color ARGBColor
     */
    fun pushBox(position: Vector2fc, size: Size<Float>, color: ARGBColor) {
        pushBox(position.x(), position.y(), size.width, size.height, color)
    }

    fun pushBox(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        topLeftColor: ARGBColor,
        topRightColor: ARGBColor,
        bottomLeftColor: ARGBColor,
        bottomRightColor: ARGBColor
    ) {
        bufferBuilder.vertex(matrices, x = x, y = y, 0f).color(topLeftColor)
        bufferBuilder.vertex(matrices, x = x, y = y + height, 0f).color(bottomLeftColor)
        bufferBuilder.vertex(matrices, x = x + width, y = y + height, 0f).color(bottomRightColor)
        bufferBuilder.vertex(matrices, x = x + width, y = y, 0f).color(topRightColor)
    }

    /**
     * 渲染一个[Box]的边框
     * @receiver RenderContext
     * @param x Float
     * @param y Float
     * @param width Float
     * @param height Float
     * @param color ARGBColor
     * @param borderSize Float
     * @param inner Boolean
     */
    fun pushBoxOutline(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: ARGBColor,
        borderSize: Float = 1f,
        inner: Boolean = false
    ) {
        check(borderSize > 0) { "borderSize must be greater than 0" }
        if (inner) {
            //top
            pushBox(x = x, y = y, width = width - borderSize, height = borderSize, color = color)
            //right
            pushBox(x = x + width - borderSize, y = y, width = borderSize, height = height - borderSize, color = color)
            //bottom
            pushBox(x = x + borderSize, y = y + height - borderSize, width = width - borderSize, height = borderSize, color = color)
            //left
            pushBox(x = x, y = y + borderSize, width = borderSize, height = height - borderSize, color = color)
        } else {
            //top
            pushBox(x = x - borderSize, y = y - borderSize, width = width + borderSize, height = borderSize, color = color)
            //right
            pushBox(x = x + width, y = y - borderSize, width = borderSize, height = height + borderSize, color = color)
            //bottom
            pushBox(x = x, y = y + height, width = width + borderSize, height = borderSize, color = color)
            //left
            pushBox(x = x - borderSize, y = y, width = borderSize, height = height + borderSize, color = color)
        }
    }

    /**
     * 渲染一个[Box]的边框
     * @param box Box
     * @param color ARGBColor
     * @param borderSize Float
     * @param inner Boolean
     */
    fun pushBoxOutline(
        box: Box,
        color: ARGBColor,
        borderSize: Float = 1f,
        inner: Boolean = false
    ) = pushBoxOutline(box.position.x(), box.position.y(), box.width, box.height, color, borderSize, inner)

    /**
     * 渲染一个[Box]的边框
     * @param transform Transform
     * @param color ARGBColor
     * @param borderSize Float
     * @param inner Boolean
     */
    fun pushBoxOutline(
        transform: Transform,
        color: ARGBColor,
        borderSize: Float = 1f,
        inner: Boolean = false
    ) = pushBoxOutline(transform.asWorldCoordinateBox, color, borderSize, inner)

    /**
     * 渲染一个渐变[Box]
     * @param x Float
     * @param y Float
     * @param width Float
     * @param height Float
     * @param startColor ARGBColor
     * @param endColor ARGBColor
     * @param orientation Orientation
     */
    fun pushGradientBox(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        startColor: ARGBColor,
        endColor: ARGBColor,
        orientation: Orientation = Orientation.Horizontal
    ) = orientation.peek(
        { pushBox(x, y, width, height, topLeftColor = startColor, topRightColor = startColor, bottomLeftColor = endColor, bottomRightColor = endColor) },
        { pushBox(x, y, width, height, topLeftColor = startColor, topRightColor = endColor, bottomLeftColor = startColor, bottomRightColor = endColor) }
    )


    /**
     * 渲染一个渐变[Box]
     * @param box Box
     * @param startColor ARGBColor
     * @param endColor ARGBColor
     * @param orientation Orientation
     */
    fun pushGradientBox(
        box: Box,
        startColor: ARGBColor,
        endColor: ARGBColor,
        orientation: Orientation = Orientation.Horizontal
    ) = pushGradientBox(box.x, box.y, box.width, box.height, startColor, endColor, orientation)

    /**
     * 渲染一个渐变[Box]
     * @param transform Transform
     * @param startColor ARGBColor
     * @param endColor ARGBColor
     * @param orientation Orientation
     */
    fun pushGradientBox(
        transform: Transform,
        startColor: ARGBColor,
        endColor: ARGBColor,
        orientation: Orientation = Orientation.Horizontal
    ) = pushGradientBox(transform.asWorldCoordinateBox, startColor, endColor, orientation)


    /**
     * 渲染一个随饱和度渐变的[Box]
     * @param x Float
     * @param y Float
     * @param width Float
     * @param height Float
     * @param orientation Orientation
     * @param reverse Boolean
     * @param saturationRange ClosedFloatingPointRange<Float>
     * @param hue Float
     * @param value Float
     * @param alpha Float
     */
    fun pushSaturationGradientBox(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        value: Float = 1f,
        alpha: Float = 1f,
    ) {
        check(saturationRange.endInclusive >= saturationRange.start) { "Saturation range must be in ascending order" }
        check(saturationRange.endInclusive in 0f..1f && saturationRange.start in 0f..1f) {
            "Saturation range must be between 0 and 1,but was ${saturationRange.start} and ${saturationRange.endInclusive}"
        }
        val colorStart = HSVColor(hue, (if (reverse) saturationRange.endInclusive else saturationRange.start).coerceIn(alphaFRange), value, alpha)
        val colorEnd = HSVColor(hue, (if (!reverse) saturationRange.endInclusive else saturationRange.start).coerceIn(alphaFRange), value, alpha)
        pushGradientBox(x, y, width, height, colorStart, colorEnd, orientation)
    }

    /**
     * 渲染一个随饱和度渐变的[Box]
     * @param box Box
     * @param orientation Orientation
     * @param reverse Boolean
     * @param saturationRange ClosedFloatingPointRange<Float>
     * @param hue Float
     * @param value Float
     * @param alpha Float
     */
    fun pushSaturationGradientBox(
        box: Box,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        value: Float = 1f,
        alpha: Float = 1f,
    ) = pushSaturationGradientBox(box.x, box.y, box.width, box.height, orientation, reverse, saturationRange, hue, value, alpha)

    /**
     * 渲染一个随饱和度渐变的[Box]
     * @param transform: Transform
     * @param orientation Orientation
     * @param reverse Boolean
     * @param saturationRange ClosedFloatingPointRange<Float>
     * @param hue Float
     * @param value Float
     * @param alpha Float
     */
    fun pushSaturationGradientBox(
        transform: Transform,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        value: Float = 1f,
        alpha: Float = 1f,
    ) = pushSaturationGradientBox(transform.asWorldCoordinateBox, orientation, reverse, saturationRange, hue, value, alpha)

    /**
     * 渲染一个随明度渐变的[Box]
     * @param x Float
     * @param y Float
     * @param width Float
     * @param height Float
     * @param orientation Orientation
     * @param reverse Boolean
     * @param valueRange ClosedFloatingPointRange<Float> 明度范围(0..1)
     * @param hue Float
     * @param saturation Float
     * @param alpha Float
     */
    fun pushValueGradientBox(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        saturation: Float = 1f,
        alpha: Float = 1f,
    ) {
        check(valueRange.endInclusive >= valueRange.start) { "Value range must be in ascending order" }
        check(valueRange.endInclusive in 0f..1f && valueRange.start in 0f..1f) {
            "Value range must be between 0 and 1,but was ${valueRange.start} and ${valueRange.endInclusive}"
        }
        val colorStart = HSVColor(hue, saturation, (if (reverse) valueRange.endInclusive else valueRange.start).coerceIn(alphaFRange), alpha)
        val colorEnd = HSVColor(hue, saturation, (if (!reverse) valueRange.endInclusive else valueRange.start).coerceIn(alphaFRange), alpha)
        pushGradientBox(x, y, width, height, colorStart, colorEnd, orientation)
    }

    /**
     * 渲染一个随明度渐变的[Box]
     * @see [renderValueGradientBox]
     * @param box Box
     * @param orientation Orientation
     * @param reverse Boolean
     * @param valueRange ClosedFloatingPointRange<Float>
     * @param hue Float
     * @param saturation Float
     * @param alpha Float
     */
    fun pushValueGradientBox(
        box: Box,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        saturation: Float = 1f,
        alpha: Float = 1f,
    ) = pushValueGradientBox(box.x, box.y, box.width, box.height, orientation, reverse, valueRange, hue, saturation, alpha)

    /**
     * 渲染一个随明度渐变的[Box]
     * @see [renderValueGradientBox]
     * @param transform: Transform
     * @param orientation Orientation
     * @param reverse Boolean
     * @param valueRange ClosedFloatingPointRange<Float>
     * @param hue Float
     * @param saturation Float
     * @param alpha Float
     */
    fun pushValueGradientBox(
        transform: Transform,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        saturation: Float = 1f,
        alpha: Float = 1f,
    ) = pushValueGradientBox(transform.asWorldCoordinateBox, orientation, reverse, valueRange, hue, saturation, alpha)


    private data class RoundBox(
        val round: Int,
        val pixelSize: Float,
        val width: Float,
        val height: Float
    )

    private val roundBoxCacheSize = 50

    private val roundBoxCache = mutableMapOf<RoundBox, Set<Pair<Vector2fc, Size<Float>>>>()

    fun pushRoundBox(
        box: Box,
        color: ARGBColor,
        round: Int,
        pixelSize: Float = 1f,
    ) {
        if (round > 0) {
            pushBox(Box(box.position + Vector2f(0f, (round + 1) * pixelSize), box.width, box.height - ((round + 1) * pixelSize) * 2), color)
            roundBoxCache[RoundBox(round, pixelSize, box.width, box.height)]?.let {
                it.forEach { (position, size) -> pushBox(Box(box.position + position, size), color) }
                return
            }
            val yPoints = mutableMapOf<Int, Int>()
            val xPoints = mutableMapOf<Int, Pair<Int, Int>>()
            pointsInCircleRange(round, round, round, 180.0..270.0).forEach { point ->
                yPoints[point.y] = yPoints[point.y]?.let { min(it, point.x) } ?: point.x
                xPoints[point.x] = xPoints[point.x]?.let { min(it.first, point.y) to it.second + 1 } ?: (point.y to 1)
            }
            buildSet {
                yPoints.map { (_, x) -> x to xPoints[x] }
                    .toSet().forEach { (x, y) ->
                        add(
                            Vector2f(abs(x * pixelSize), abs(y!!.first) * pixelSize) to
                                    Size(box.width - abs(x * pixelSize * 2), y.second * pixelSize)
                        )
                        add(
                            Vector2f(abs(x * pixelSize), box.height - y.second * pixelSize - (abs(y.first)) * pixelSize) to
                                    Size(box.width - abs(x * pixelSize * 2), y.second * pixelSize)
                        )
                    }
                roundBoxCache[RoundBox(round, pixelSize, box.width, box.height)] = this
                if (size > roundBoxCacheSize) roundBoxCache.remove(roundBoxCache.keys.first())
            }.forEach { (position, size) -> pushBox(Box(box.position + position, size), color) }
        } else pushBox(box, color)
    }

    fun pushRoundBox(
        transform: Transform,
        color: ARGBColor,
        round: Int,
        pixelSize: Float = 1f,
    ) = pushRoundBox(transform.asWorldCoordinateBox, color, round, pixelSize)

}