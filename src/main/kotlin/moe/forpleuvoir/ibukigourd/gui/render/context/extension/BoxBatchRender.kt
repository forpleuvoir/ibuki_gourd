@file:Suppress("DuplicatedCode", "unused")

package moe.forpleuvoir.ibukigourd.gui.render.context.extension

import moe.forpleuvoir.ibukigourd.gui.render.Size
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.render.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext
import moe.forpleuvoir.ibukigourd.gui.render.helper.*
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.ColoredBox
import moe.forpleuvoir.ibukigourd.gui.render.shape.pointsInCircleRange
import moe.forpleuvoir.ibukigourd.render.*
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.render.math.plus
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.common.color.alphaFRange
import moe.forpleuvoir.nebula.common.pick
import moe.forpleuvoir.nebula.common.util.clamp
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import org.joml.Vector2fc
import kotlin.math.abs
import kotlin.math.min

fun RenderContext.batchRenderBox(
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram,
    block: BoxBatchRenderScope.() -> Unit
) {
    setShader(shaderSupplier)
    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
    block(BoxBatchRenderScope)
    bufferBuilder.draw()
}

@Suppress("MemberVisibilityCanBePrivate")
open class BoxBatchRenderScope private constructor() {

    internal companion object : BoxBatchRenderScope()

    /**
     * 渲染一个[Box]
     * @receiver RenderContext
     * @param box Box
     * @param color ARGBColor
     */
    fun RenderContext.box(box: Box, color: ARGBColor) {
        for (vertex in box.vertexes) {
            bufferBuilder.vertex(matrixStack, vertex).color(color).next()
        }
    }

    /**
     * 渲染一个[ColoredBox]
     * @receiver RenderContext
     * @param coloredBox ColoredBox
     */
    fun RenderContext.box(coloredBox: ColoredBox) {
        for (vertex in coloredBox.coloredVertexes) {
            bufferBuilder.vertex(matrixStack, vertex).color(vertex.color).next()
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
    fun RenderContext.box(x: Float, y: Float, width: Float, height: Float, color: ARGBColor) {
        bufferBuilder.vertex(matrixStack, x = x, y = y, 0f).color(color).next()
        bufferBuilder.vertex(matrixStack, x = x + width, y = y, 0f).color(color).next()
        bufferBuilder.vertex(matrixStack, x = x + width, y = y + height, 0f).color(color).next()
        bufferBuilder.vertex(matrixStack, x = x, y = y + height, 0f).color(color).next()
    }

    /**
     * 渲染一个[Box]
     * @receiver RenderContext
     * @param position Vector2fc
     * @param size Size<Float>
     * @param color ARGBColor
     */
    fun RenderContext.box(position: Vector2fc, size: Size<Float>, color: ARGBColor) {
        box(position.x(), position.y(), size.width, size.height, color)
    }

    fun RenderContext.box(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        topLeftColor: ARGBColor,
        topRightColor: ARGBColor,
        bottomLeftColor: ARGBColor,
        bottomRightColor: ARGBColor
    ) {
        bufferBuilder.vertex(matrixStack, x = x, y = y, 0f).color(topLeftColor).next()
        bufferBuilder.vertex(matrixStack, x = x + width, y = y, 0f).color(topRightColor).next()
        bufferBuilder.vertex(matrixStack, x = x + width, y = y + height, 0f).color(bottomRightColor).next()
        bufferBuilder.vertex(matrixStack, x = x, y = y + height, 0f).color(bottomLeftColor).next()
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
    fun RenderContext.boxOutline(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: ARGBColor,
        borderSize: Float = 1f,
        inner: Boolean = false
    ) {
        check(borderSize > 0) { "borderSize must be greater than 0" }
        val offset = if (inner) 0f else -borderSize
        //top
        box(x = x, y = y + offset, width = width - borderSize, height = borderSize, color = color)
        //right
        box(x = x + width, y = y + offset, width = borderSize, height = height - borderSize, color = color)
        //bottom
        box(x = x + borderSize, y = y + height + if (inner) -borderSize else 0f, width = width - borderSize, height = borderSize, color = color)
        //left
        box(x = x + offset, y = y + borderSize, width = borderSize, height = height - borderSize, color = color)
    }

    /**
     * 渲染一个[Box]的边框
     * @receiver RenderContext
     * @param box Box
     * @param color ARGBColor
     * @param borderSize Float
     * @param inner Boolean
     */
    fun RenderContext.boxOutline(
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
    fun RenderContext.gradientBox(
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
    fun RenderContext.gradientBox(
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
    fun RenderContext.hueGradientBox(
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
                hue = (hue + hueOffset).clamp(hueRange)
                val colorEnd = HSVColor(hue, saturation, value, alpha, false)

                repeat(precision) {
                    box(x, currentY, width, lengthSlice, colorStart, colorEnd, colorEnd, colorStart)
                    colorStart.hue = hue
                    hue = (hue + hueOffset).clamp(hueRange)
                    colorEnd.hue = hue
                    currentY += lengthSlice
                }
            }, {
                val lengthSlice = width / precision
                var currentX = x

                val colorStart = HSVColor(hue, saturation, value, alpha, false)
                hue = (hue + hueOffset).clamp(hueRange)
                val colorEnd = HSVColor(hue, saturation, value, alpha, false)

                repeat(precision) {
                    box(currentX, y, width, lengthSlice, colorStart, colorStart, colorEnd, colorEnd)
                    colorStart.hue = hue
                    hue = (hue + hueOffset).clamp(hueRange)
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
    fun RenderContext.hueGradientBox(
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
    fun RenderContext.saturationGradientBox(
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
        val colorStart = HSVColor(hue, (if (reverse) saturationRange.endInclusive else saturationRange.start).clamp(alphaFRange), value, alpha)
        val colorEnd = HSVColor(hue, (if (!reverse) saturationRange.endInclusive else saturationRange.start).clamp(alphaFRange), value, alpha)
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
    fun RenderContext.saturationGradientBox(
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
    fun RenderContext.valueGradientBox(
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
        val colorStart = HSVColor(hue, saturation, (if (reverse) valueRange.endInclusive else valueRange.start).clamp(alphaFRange), alpha)
        val colorEnd = HSVColor(hue, saturation, (if (!reverse) valueRange.endInclusive else valueRange.start).clamp(alphaFRange), alpha)
        gradientBox(x, y, width, height, colorStart, colorEnd, orientation)
    }

    /**
     * 渲染一个随明度渐变的[Box]
     * @see [renderValueGradientRect]
     * @receiver RenderContext
     * @param box Box
     * @param orientation Orientation
     * @param reverse Boolean
     * @param valueRange ClosedFloatingPointRange<Float>
     * @param hue Float
     * @param saturation Float
     * @param alpha Float
     */
    fun RenderContext.valueGradientBox(
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

    fun RenderContext.roundBox(
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