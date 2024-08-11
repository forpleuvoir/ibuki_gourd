@file:Suppress("DuplicatedCode", "unused")

package moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.ColoredBox
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.pointsInCircleRange
import moe.forpleuvoir.ibukigourd.render.color
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.render.math.plus
import moe.forpleuvoir.ibukigourd.render.setShader
import moe.forpleuvoir.ibukigourd.render.vertex
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.common.color.alphaFRange
import moe.forpleuvoir.nebula.common.util.primitive.pick
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import org.joml.Vector2fc
import kotlin.math.abs
import kotlin.math.min

fun DrawContext.batchRenderBox(
    layer: RenderLayer = RenderLayer.getGui(),
    shaderSupplier: (() -> ShaderProgram?)? = GameRenderer::getPositionColorProgram,
    block: BoxBatchRenderScope.() -> Unit
) {
    setShader(shaderSupplier)
    block(BoxBatchRenderScope(vertexConsumers.getBuffer(layer)))
    draw()
}

@Suppress("MemberVisibilityCanBePrivate")
data class BoxBatchRenderScope internal constructor(val bufferBuilder: VertexConsumer) {

    /**
     * 渲染一个[Box]
     * @receiver RenderContext
     * @param box Box
     * @param color ARGBColor
     */
    fun DrawContext.box(box: Box, color: ARGBColor) {
        for (vertex in box.vertexes) {
            bufferBuilder.vertex(matrices, vertex).color(color)
        }
    }

    /**
     * 渲染一个[ColoredBox]
     * @receiver RenderContext
     * @param coloredBox ColoredBox
     */
    fun DrawContext.box(coloredBox: ColoredBox) {
        for (vertex in coloredBox.coloredVertexes) {
            bufferBuilder.vertex(matrices, vertex).color(vertex.color)
        }
    }

    /**
     * 渲染一个[Box]
     * @receiver RenderContext
     * @param x Number
     * @param y Number
     * @param width Number
     * @param height Number
     * @param color ARGBColor
     */
    fun DrawContext.box(x: Float, y: Float, width: Float, height: Float, color: ARGBColor) {
        bufferBuilder.vertex(matrices, x = x, y = y, 0f).color(color)
        bufferBuilder.vertex(matrices, x = x, y = y + height, 0f).color(color)
        bufferBuilder.vertex(matrices, x = x + width, y = y + height, 0f).color(color)
        bufferBuilder.vertex(matrices, x = x + width, y = y, 0f).color(color)
    }

    /**
     * 渲染一个[Box]
     * @receiver RenderContext
     * @param position Vector2fc
     * @param size Size<Float>
     * @param color ARGBColor
     */
    fun DrawContext.box(position: Vector2fc, size: Size<Float>, color: ARGBColor) {
        box(position.x(), position.y(), size.width, size.height, color)
    }

    fun DrawContext.box(
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
    fun DrawContext.boxOutline(
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
            box(x = x, y = y, width = width - borderSize, height = borderSize, color = color)
            //right
            box(x = x + width - borderSize, y = y, width = borderSize, height = height - borderSize, color = color)
            //bottom
            box(x = x + borderSize, y = y + height - borderSize, width = width - borderSize, height = borderSize, color = color)
            //left
            box(x = x, y = y + borderSize, width = borderSize, height = height - borderSize, color = color)
        } else {
            //top
            box(x = x - borderSize, y = y - borderSize, width = width + borderSize, height = borderSize, color = color)
            //right
            box(x = x + width, y = y - borderSize, width = borderSize, height = height + borderSize, color = color)
            //bottom
            box(x = x, y = y + height, width = width + borderSize, height = borderSize, color = color)
            //left
            box(x = x - borderSize, y = y, width = borderSize, height = height + borderSize, color = color)
        }
    }

    /**
     * 渲染一个[Box]的边框
     * @receiver RenderContext
     * @param box Box
     * @param color ARGBColor
     * @param borderSize Float
     * @param inner Boolean
     */
    fun DrawContext.boxOutline(
        box: Box,
        color: ARGBColor,
        borderSize: Float = 1f,
        inner: Boolean = false
    ) {
        boxOutline(box.position.x(), box.position.y(), box.width, box.height, color, borderSize, inner)
    }

    /**
     * 渲染一个渐变[Box]
     * @receiver RenderContext
     * @param x Float
     * @param y Float
     * @param width Float
     * @param height Float
     * @param startColor ARGBColor
     * @param endColor ARGBColor
     * @param orientation Orientation
     */
    fun DrawContext.gradientBox(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        startColor: ARGBColor,
        endColor: ARGBColor,
        orientation: Orientation = Orientation.Horizontal
    ) {
        orientation.peek(
            box(x, y, width, height, topLeftColor = startColor, topRightColor = startColor, bottomLeftColor = endColor, bottomRightColor = endColor),
            box(x, y, width, height, topLeftColor = startColor, topRightColor = endColor, bottomLeftColor = startColor, bottomRightColor = endColor)
        )
    }

    /**
     * 渲染一个渐变[Box]
     * @receiver RenderContext
     * @param box Box
     * @param startColor ARGBColor
     * @param endColor ARGBColor
     * @param orientation Orientation
     */
    fun DrawContext.gradientBox(
        box: Box,
        startColor: ARGBColor,
        endColor: ARGBColor,
        orientation: Orientation = Orientation.Horizontal
    ) {
        gradientBox(box.x, box.y, box.width, box.height, startColor, endColor, orientation)
    }

    /**
     * 渲染一个随色相渐变的[Box]
     * @receiver RenderContext
     * @param x Float
     * @param y Float
     * @param width Float
     * @param height Float
     * @param precision Int 精度,精度越高效果越好,性能消耗越大
     * @param orientation Orientation
     * @param reverse Boolean
     * @param hueRange ClosedFloatingPointRange<Float> 色相范围(0..360)
     * @param saturation Float
     * @param value Float
     * @param alpha Float
     */
    fun DrawContext.hueGradientBox(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        precision: Int,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        hueRange: ClosedFloatingPointRange<Float> = 0f..360f,
        saturation: Float = 1f,
        value: Float = 1f,
        alpha: Float = 1f,
    ) {
        check(hueRange.start < hueRange.endInclusive) { "Hue range must be in ascending order" }
        check(hueRange.start in 0f..360f && hueRange.endInclusive in 0f..360f) {
            "Hue range must be between 0 and 360, but was ${hueRange.start} and ${hueRange.endInclusive}"
        }
        val hueSlice = abs(hueRange.endInclusive - hueRange.start) / precision
        var hue = if (reverse) hueRange.endInclusive else hueRange.start
        val hueOffset = reverse.pick(-hueSlice, hueSlice)
        orientation.peek(
            {
                val lengthSlice = height / precision
                var currentY = y

                val colorStart = HSVColor(hue, saturation, value, alpha, false)
                hue = (hue + hueOffset).coerceIn(hueRange)
                val colorEnd = HSVColor(hue, saturation, value, alpha, false)

                repeat(precision) {
                    box(x, currentY, width, lengthSlice, colorStart, colorEnd, colorEnd, colorStart)
                    colorStart.hue = hue
                    hue = (hue + hueOffset).coerceIn(hueRange)
                    colorEnd.hue = hue
                    currentY += lengthSlice
                }
            }, {
                val lengthSlice = width / precision
                var currentX = x

                val colorStart = HSVColor(hue, saturation, value, alpha, false)
                hue = (hue + hueOffset).coerceIn(hueRange)
                val colorEnd = HSVColor(hue, saturation, value, alpha, false)

                repeat(precision) {
                    box(currentX, y, lengthSlice, height, colorStart, colorStart, colorEnd, colorEnd)
                    colorStart.hue = hue
                    hue = (hue + hueOffset).coerceIn(hueRange)
                    colorEnd.hue = hue
                    currentX += lengthSlice
                }
            }
        )
    }

    /**
     * @see [hueGradientBox]
     * @receiver RenderContext
     * @param box Box
     * @param precision Int
     * @param orientation Orientation
     * @param reverse Boolean
     * @param hueRange ClosedFloatingPointRange<Float>
     * @param saturation Float
     * @param value Float
     * @param alpha Float
     */
    fun DrawContext.hueGradientBox(
        box: Box,
        precision: Int,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        hueRange: ClosedFloatingPointRange<Float> = 0f..360f,
        saturation: Float = 1f,
        value: Float = 1f,
        alpha: Float = 1f,
    ) {
        hueGradientBox(box.x, box.y, box.width, box.height, precision, orientation, reverse, hueRange, saturation, value, alpha)
    }

    /**
     * 渲染一个随饱和度渐变的[Box]
     * @receiver RenderContext
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
    fun DrawContext.saturationGradientBox(
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
        gradientBox(x, y, width, height, colorStart, colorEnd, orientation)
    }

    /**
     * 渲染一个随饱和度渐变的[Box]
     * @receiver RenderContext
     * @param box Box
     * @param orientation Orientation
     * @param reverse Boolean
     * @param saturationRange ClosedFloatingPointRange<Float>
     * @param hue Float
     * @param value Float
     * @param alpha Float
     */
    fun DrawContext.saturationGradientBox(
        box: Box,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        value: Float = 1f,
        alpha: Float = 1f,
    ) {
        saturationGradientBox(box.x, box.y, box.width, box.height, orientation, reverse, saturationRange, hue, value, alpha)
    }

    /**
     * 渲染一个随明度渐变的[Box]
     * @receiver RenderContext
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
    fun DrawContext.valueGradientBox(
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
        gradientBox(x, y, width, height, colorStart, colorEnd, orientation)
    }

    /**
     * 渲染一个随明度渐变的[Box]
     * @see [renderValueGradientBox]
     * @receiver RenderContext
     * @param box Box
     * @param orientation Orientation
     * @param reverse Boolean
     * @param valueRange ClosedFloatingPointRange<Float>
     * @param hue Float
     * @param saturation Float
     * @param alpha Float
     */
    fun DrawContext.valueGradientBox(
        box: Box,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        saturation: Float = 1f,
        alpha: Float = 1f,
    ) {
        valueGradientBox(box.x, box.y, box.width, box.height, orientation, reverse, valueRange, hue, saturation, alpha)
    }

    private data class RoundBox(
        val round: Int,
        val pixelSize: Float,
        val width: Float,
        val height: Float
    )

    private val roundBoxCacheSize = 50

    private val roundBoxCache = mutableMapOf<RoundBox, Set<Pair<Vector2fc, Size<Float>>>>()

    fun DrawContext.roundBox(
        rect: Box,
        color: ARGBColor,
        round: Int,
        pixelSize: Float = 1f,
    ) {
        if (round > 0) {
            box(Box(rect.position + Vector2f(0f, (round + 1) * pixelSize), rect.width, rect.height - ((round + 1) * pixelSize) * 2), color)
            roundBoxCache[RoundBox(round, pixelSize, rect.width, rect.height)]?.let {
                it.forEach { (position, size) -> box(Box(rect.position + position, size), color) }
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
                                    Size(rect.width - abs(x * pixelSize * 2), y.second * pixelSize)
                        )
                        add(
                            Vector2f(abs(x * pixelSize), rect.height - y.second * pixelSize - (abs(y.first)) * pixelSize) to
                                    Size(rect.width - abs(x * pixelSize * 2), y.second * pixelSize)
                        )
                    }
                roundBoxCache[RoundBox(round, pixelSize, rect.width, rect.height)] = this
                if (size > roundBoxCacheSize) roundBoxCache.remove(roundBoxCache.keys.first())
            }.forEach { (position, size) -> box(Box(rect.position + position, size), color) }
        } else box(rect, color)
    }

}