@file:Suppress("unused", "DuplicatedCode")

package moe.forpleuvoir.ibukigourd.render.helper

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.render.Size
import moe.forpleuvoir.ibukigourd.gui.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.render.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.render.shape.box.ColoredBox
import moe.forpleuvoir.ibukigourd.gui.render.shape.pointsInCircleRange
import moe.forpleuvoir.ibukigourd.gui.render.vertex.ColoredVertex
import moe.forpleuvoir.ibukigourd.render.*
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.render.math.plus
import moe.forpleuvoir.nebula.common.color.*
import moe.forpleuvoir.nebula.common.util.clamp
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import org.joml.Vector2fc
import org.joml.Vector3fc
import kotlin.math.abs
import kotlin.math.min

/**
 * 渲染线段
 * @param matrixStack MatrixStack
 * @param lineWidth Number
 * @param vertex1 ColorVertex
 * @param vertex2 ColorVertex
 */
fun renderLine(matrixStack: MatrixStack, lineWidth: Number, vertex1: ColoredVertex, vertex2: ColoredVertex, normal: Vector3fc) {
    setShader(GameRenderer::getRenderTypeLinesProgram)
    enableBlend()
    defaultBlendFunc()
    lineWidth(lineWidth)
    bufferBuilder.begin(VertexFormat.DrawMode.LINE_STRIP, VertexFormats.LINES)
    bufferBuilder.vertex(matrixStack, vertex1).color(vertex1.color).normal(normal).next()
    bufferBuilder.vertex(matrixStack, vertex2).color(vertex2.color).normal(normal).next()
    bufferBuilder.draw()
    lineWidth(1f)
    disableBlend()
}

/**
 * 渲染线段
 * @param matrixStack MatrixStack
 * @param lineWidth Number
 * @param vertexes [List]<[ColoredVertex]>
 */
fun renderLine(matrixStack: MatrixStack, lineWidth: Number, vararg vertexes: ColoredVertex) {
    setShader(GameRenderer::getPositionColorProgram)
    enableBlend()
    defaultBlendFunc()
    lineWidth(lineWidth)
    bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR)
    for (vertex in vertexes) {
        bufferBuilder.vertex(matrixStack, vertex).color(vertex.color).next()
    }
    bufferBuilder.draw()
    lineWidth(1f)
    disableBlend()
}

/**
 * 批量绘制矩形
 * @param shaderSupplier () -> ShaderProgram?
 * @param bufferBuilder BufferBuilder
 * @param block BatchDrawScope.() -> Unit
 */
fun rectBatchRender(
    bufferBuilder: BufferBuilder = moe.forpleuvoir.ibukigourd.render.bufferBuilder,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram,
    block: RectBatchDrawScope.() -> Unit
) {
    enableBlend()
    setShader(shaderSupplier)
    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
    RectBatchDrawScope.vertexConsumer = bufferBuilder
    block(RectBatchDrawScope)
    bufferBuilder.draw()
    disableBlend()
    RectBatchDrawScope.vertexConsumer = null
}

object RectBatchDrawScope {

    var vertexConsumer: VertexConsumer? = null
        internal set

    /**
     * 渲染矩形
     * @param matrixStack MatrixStack
     * @param rect Rectangle
     */
    fun renderRect(matrixStack: MatrixStack, rect: ColoredBox) {
        for (vertex in rect.coloredVertexes) {
            vertexConsumer!!.vertex(matrixStack, vertex).color(vertex.color).next()
        }
    }

    /**
     * 渲染矩形
     * @param matrixStack MatrixStack
     * @param rect Rectangle
     * @param color ARGBColor
     */
    fun renderRect(matrixStack: MatrixStack, rect: Box, color: ARGBColor) {
        for (vertex in rect.vertexes) {
            vertexConsumer!!.vertex(matrixStack, vertex).color(color).next()
        }
    }

    /**
     * 渲染矩形
     * @param matrixStack MatrixStack
     * @param coloredVertex Vertex
     * @param width Number
     * @param height Number
     */
    fun renderRect(matrixStack: MatrixStack, coloredVertex: ColoredVertex, width: Number, height: Number) {
        renderRect(matrixStack, ColoredBox(coloredVertex, width.toFloat(), height.toFloat()))
    }

    /**
     * @see renderRect
     * @param matrixStack MatrixStack
     * @param transform Transform
     * @param color Color
     */
    fun renderRect(matrixStack: MatrixStack, transform: Transform, color: ARGBColor) =
        renderRect(matrixStack, ColoredVertex(transform.worldPosition, color), transform.width, transform.height)

    /**
     * Renders a round rectangle with the specified parameters.
     *
     * @param matrixStack The MatrixStack used for rendering.
     * @param rect The rectangle representing the dimensions and position of the round rectangle.
     * @param color The color of the round rectangle in ARGB format.
     * @param round The radius of the round corners of the rectangle.
     * @param pixelSize The size in pixels of each unit in the coordinate system. Default value is 1.0 pixel.
     */
    fun renderRoundRect(matrixStack: MatrixStack, rect: Box, color: ARGBColor, round: Int, pixelSize: Float = 1f) {
        if (round > 0) {
            renderRect(matrixStack, Box(rect.position + Vector2f(0f, (round + 1) * pixelSize), rect.width, rect.height - ((round + 1) * pixelSize) * 2), color)
            renderRoundRectCache[RenderRoundRect(round, pixelSize, rect.width, rect.height)]?.let {
                it.forEach { (position, size) -> renderRect(matrixStack, Box(rect.position + position, size), color) }
                return
            }
            val yPoints = mutableMapOf<Int, Int>()
            val xPoints = mutableMapOf<Int, Pair<Int, Int>>()
            pointsInCircleRange(round, round, round, 180.0..270.0).forEach { point ->
                yPoints[point.y] = yPoints[point.y]?.let { min(it, point.x) } ?: point.x
                xPoints[point.x] = xPoints[point.x]?.let { min(it.first, point.y) to it.second + 1 } ?: (point.y to 1)
            }
            buildSet {
                yPoints.map { (_, x) -> x to xPoints[x] }.toSet().forEach { (x, y) ->
                    add(Vector2f(abs(x * pixelSize), abs(y!!.first) * pixelSize) to Size(rect.width - abs(x * pixelSize * 2), y.second * pixelSize))
                    add(
                        Vector2f(abs(x * pixelSize), rect.height - y.second * pixelSize - (abs(y.first)) * pixelSize)
                                to Size(rect.width - abs(x * pixelSize * 2), y.second * pixelSize)
                    )
                }
                renderRoundRectCache[RenderRoundRect(round, pixelSize, rect.width, rect.height)] = this
                if (size > renderRoundRectCacheSize) renderRoundRectCache.remove(renderRoundRectCache.keys.first())
            }.forEach { (position, size) -> renderRect(matrixStack, Box(rect.position + position, size), color) }
        } else renderRect(matrixStack, rect, color)
    }

    /**
     * Renders a gradient rectangle onto the given matrix stack.
     *
     * @param matrixStack The matrix stack to render onto.
     * @param rect The rectangle to render.
     * @param orientation The arrangement of gradient colors. Default value is [Orientation.Horizontal].
     * @param startColor The starting color of the gradient in ARGB format.
     * @param endColor The ending color of the gradient in ARGB format.
     */
    fun renderGradientRect(
        matrixStack: MatrixStack,
        rect: Box,
        orientation: Orientation = Orientation.Horizontal,
        startColor: ARGBColor,
        endColor: ARGBColor,
    ) {
        orientation.peek(
            renderRect(matrixStack, ColoredBox(rect, startColor, endColor, endColor, startColor)),
            renderRect(matrixStack, ColoredBox(rect, startColor, startColor, endColor, endColor))
        )
    }

    /**
     * 渲染HSV渐变矩形
     * @param matrixStack MatrixStack
     * @param rect Rectangle
     * @param precision Int 精度
     * @param orientation [Orientation]
     * @param reverse Boolean
     * @param hueRange ClosedFloatingPointRange<Float>
     * @param saturation Float
     * @param value Float
     * @param alpha Float
     */
    fun renderHueGradientRect(
        matrixStack: MatrixStack,
        rect: Box,
        precision: Int,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        hueRange: ClosedFloatingPointRange<Float> = 0f..360f,
        saturation: Float = 1f,
        value: Float = 1f,
        alpha: Float = 1f,
    ) {
        val hueSlice = hueRange.endInclusive / precision
        var hue = if (reverse) hueRange.endInclusive else hueRange.start
        orientation.peek(
            {
                val lengthSlice = rect.height / precision
                var y = rect.y
                for (i in 0 until precision) {
                    val colorStart = HSVColor(hue, saturation, value, alpha, false)
                    hue = if (reverse) (hue - hueSlice).clamp(hueRange) else (hue + hueSlice).clamp(hueRange)
                    val colorEnd = HSVColor(hue, saturation, value, alpha, false)
                    renderRect(matrixStack, ColoredBox(rect.x, y, Size(rect.width, lengthSlice), colorStart, colorEnd, colorEnd, colorStart))
                    y += lengthSlice
                }

            }, {
                val lengthSlice = rect.width / precision
                var x = rect.x
                for (i in 0 until precision) {
                    val colorStart = HSVColor(hue, saturation, value, alpha, false)
                    hue = if (reverse) (hue - hueSlice).clamp(hueRange) else (hue + hueSlice).clamp(hueRange)
                    val colorEnd = HSVColor(hue, saturation, value, alpha, false)
                    renderRect(matrixStack, ColoredBox(x, rect.y, Size(lengthSlice, rect.height), colorStart, colorStart, colorEnd, colorEnd))
                    x += lengthSlice
                }
            }
        )
    }

    /**
     * 绘制饱和度渐变矩形
     * @param matrixStack MatrixStack
     * @param rect Rectangle
     * @param orientation [Orientation]
     * @param reverse Boolean
     * @param saturationRange ClosedFloatingPointRange<Float>
     * @param hue Float
     * @param value Float
     * @param alpha Float
     */
    fun renderSaturationGradientRect(
        matrixStack: MatrixStack,
        rect: Box,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        value: Float = 1f,
        alpha: Float = 1f,
    ) {
        val colorStart = HSVColor(hue, (if (reverse) saturationRange.endInclusive else saturationRange.start).clamp(alphaFRange), value, alpha)
        val colorEnd = HSVColor(hue, (if (!reverse) saturationRange.endInclusive else saturationRange.start).clamp(alphaFRange), value, alpha)
        renderGradientRect(matrixStack, rect, orientation, colorStart, colorEnd)
    }

    /**
     * 绘制明度渐变矩形
     * @param matrixStack MatrixStack
     * @param rect Rectangle
     * @param orientation [Orientation]
     * @param reverse Boolean
     * @param valueRange ClosedFloatingPointRange<Float>
     * @param hue Float
     * @param saturation Float
     * @param alpha Float
     */
    fun renderValueGradientRect(
        matrixStack: MatrixStack,
        rect: Box,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
        saturation: Float = 1f,
        alpha: Float = 1f,
    ) {
        val colorStart = HSVColor(hue, saturation, (if (reverse) valueRange.endInclusive else valueRange.start).clamp(alphaFRange), alpha)
        val colorEnd = HSVColor(hue, saturation, (if (!reverse) valueRange.endInclusive else valueRange.start).clamp(alphaFRange), alpha)
        renderGradientRect(matrixStack, rect, orientation, colorStart, colorEnd)
    }

    /**
     * 绘制透明度渐变矩形
     * @param matrixStack MatrixStack
     * @param rect Rectangle
     * @param orientation [Orientation]
     * @param reverse Boolean
     * @param alphaRange ClosedFloatingPointRange<Float>
     * @param color ARGBColor
     */
    fun renderAlphaGradientRect(
        matrixStack: MatrixStack,
        rect: Box,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        alphaRange: ClosedFloatingPointRange<Float> = 0f..1f,
        color: ARGBColor,
    ) {
        val colorStart = Color(color.argb).alpha((if (reverse) alphaRange.endInclusive else alphaRange.start).clamp(alphaFRange))
        val colorEnd = Color(color.argb).alpha((if (!reverse) alphaRange.endInclusive else alphaRange.start).clamp(alphaFRange))
        renderGradientRect(matrixStack, rect, orientation, colorStart, colorEnd)
    }

    /**
     * 绘制饱和度和明度渐变矩形
     * @param matrixStack MatrixStack
     * @param rect Rectangle
     * @param orientation [Orientation]
     * @param reverse Boolean
     * @param saturationRange ClosedFloatingPointRange<Float>
     * @param valueRange ClosedFloatingPointRange<Float>
     * @param hue Float
     */
    fun renderSVGradientRect(
        matrixStack: MatrixStack,
        rect: Box,
        orientation: Orientation = Orientation.Horizontal,
        reverse: Boolean = false,
        saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        hue: Float = 360f,
    ) {
        val saturationStart = saturationRange.start
        val saturationEnd = saturationRange.endInclusive
        val valueStart = valueRange.start
        val valueEnd = valueRange.endInclusive
        val saturationStartColor = HSVColor(hue, if (reverse) saturationEnd else saturationStart, valueEnd)
        val saturationEndColor = HSVColor(hue, if (!reverse) saturationEnd else saturationStart, valueEnd)
        val alphaStartColor = Colors.BLACK.alpha((if (reverse) valueEnd else valueStart).clamp(valueRange))
        val alphaEndColor = Colors.BLACK.alpha((if (!reverse) valueEnd else valueStart).clamp(valueRange))
        orientation.peek(
            {
                renderGradientRect(matrixStack, rect, orientation, saturationStartColor, saturationEndColor)
                renderGradientRect(matrixStack, rect, Orientation.Horizontal, alphaStartColor, alphaEndColor)
            }, {
                renderGradientRect(matrixStack, rect, orientation, saturationStartColor, saturationEndColor)
                renderGradientRect(matrixStack, rect, Orientation.Vertical, alphaStartColor, alphaEndColor)
            }
        )
    }

}

/**
 * Renders a gradient rectangle onto the given matrix stack.
 *
 * @param matrixStack The matrix stack to render onto.
 * @param rect The rectangle to render.
 * @param orientation The arrangement of gradient colors. Default value is [Orientation.Horizontal].
 * @param startColor The starting color of the gradient in ARGB format.
 * @param endColor The ending color of the gradient in ARGB format.
 */
fun renderGradientRect(
    matrixStack: MatrixStack,
    rect: Box,
    orientation: Orientation = Orientation.Horizontal,
    startColor: ARGBColor,
    endColor: ARGBColor,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram
) {
    orientation.peek(
        renderRect(matrixStack, ColoredBox(rect, startColor, endColor, endColor, startColor), shaderSupplier),
        renderRect(matrixStack, ColoredBox(rect, startColor, startColor, endColor, endColor), shaderSupplier)
    )
}

/**
 * 渲染HSV渐变矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle
 * @param precision Int 精度
 * @param orientation [Orientation]
 * @param reverse Boolean
 * @param hueRange ClosedFloatingPointRange<Float>
 * @param saturation Float
 * @param value Float
 * @param alpha Float
 */
fun renderHueGradientRect(
    matrixStack: MatrixStack,
    rect: Box,
    precision: Int,
    orientation: Orientation = Orientation.Horizontal,
    reverse: Boolean = false,
    hueRange: ClosedFloatingPointRange<Float> = 0f..360f,
    saturation: Float = 1f,
    value: Float = 1f,
    alpha: Float = 1f,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram
) {
    val hueSlice = hueRange.endInclusive / precision
    var hue = if (reverse) hueRange.endInclusive else hueRange.start
    rectBatchRender(shaderSupplier = shaderSupplier) {
        orientation.peek(
            {
                val lengthSlice = rect.height / precision
                var y = rect.y
                for (i in 0 until precision) {
                    val colorStart = HSVColor(hue, saturation, value, alpha, false)
                    hue = if (reverse) (hue - hueSlice).clamp(hueRange) else (hue + hueSlice).clamp(hueRange)
                    val colorEnd = HSVColor(hue, saturation, value, alpha, false)
                    renderRect(matrixStack, ColoredBox(rect.x, y, Size(rect.width, lengthSlice), colorStart, colorEnd, colorEnd, colorStart))
                    y += lengthSlice
                }

            },
            {
                val lengthSlice = rect.width / precision
                var x = rect.x
                for (i in 0 until precision) {
                    val colorStart = HSVColor(hue, saturation, value, alpha, false)
                    hue = if (reverse) (hue - hueSlice).clamp(hueRange) else (hue + hueSlice).clamp(hueRange)
                    val colorEnd = HSVColor(hue, saturation, value, alpha, false)
                    renderRect(matrixStack, ColoredBox(x, rect.y, Size(lengthSlice, rect.height), colorStart, colorStart, colorEnd, colorEnd))
                    x += lengthSlice
                }
            }
        )
    }
}

/**
 * 绘制饱和度渐变矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle
 * @param orientation [Orientation]
 * @param reverse Boolean
 * @param saturationRange ClosedFloatingPointRange<Float>
 * @param hue Float
 * @param value Float
 * @param alpha Float
 */
fun renderSaturationGradientRect(
    matrixStack: MatrixStack,
    rect: Box,
    orientation: Orientation = Orientation.Horizontal,
    reverse: Boolean = false,
    saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 360f,
    value: Float = 1f,
    alpha: Float = 1f,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram
) {
    val colorStart = HSVColor(hue, (if (reverse) saturationRange.endInclusive else saturationRange.start).clamp(alphaFRange), value, alpha)
    val colorEnd = HSVColor(hue, (if (!reverse) saturationRange.endInclusive else saturationRange.start).clamp(alphaFRange), value, alpha)
    renderGradientRect(matrixStack, rect, orientation, colorStart, colorEnd, shaderSupplier)
}

/**
 * 绘制明度渐变矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle
 * @param orientation [Orientation]
 * @param reverse Boolean
 * @param valueRange ClosedFloatingPointRange<Float>
 * @param hue Float
 * @param saturation Float
 * @param alpha Float
 */
fun renderValueGradientRect(
    matrixStack: MatrixStack,
    rect: Box,
    orientation: Orientation = Orientation.Horizontal,
    reverse: Boolean = false,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 360f,
    saturation: Float = 1f,
    alpha: Float = 1f,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram
) {
    val colorStart = HSVColor(hue, saturation, (if (reverse) valueRange.endInclusive else valueRange.start).clamp(alphaFRange), alpha)
    val colorEnd = HSVColor(hue, saturation, (if (!reverse) valueRange.endInclusive else valueRange.start).clamp(alphaFRange), alpha)
    renderGradientRect(matrixStack, rect, orientation, colorStart, colorEnd, shaderSupplier)
}

/**
 * Renders a rectangular shape with a gradient of alpha values.
 *
 * @param matrixStack The matrix stack used for rendering.
 * @param rect The rectangle representing the shape to be rendered.
 * @param orientation The arrangement of the gradient. Default is [Orientation.Horizontal].
 * @param reverse Whether to reverse the gradient. Default is false.
 * @param alphaRange The range of alpha values for the gradient. Default is 0f to 1f.
 * @param color The base color of the shape.
 * @param shaderSupplier The supplier function for the shader program to be used for rendering. Default is [GameRenderer.getPositionColorProgram].
 */
fun renderAlphaGradientRect(
    matrixStack: MatrixStack,
    rect: Box,
    orientation: Orientation = Orientation.Horizontal,
    reverse: Boolean = false,
    alphaRange: ClosedFloatingPointRange<Float> = 0f..1f,
    color: ARGBColor,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram
) {
    val colorStart = Color(color.argb).alpha((if (reverse) alphaRange.endInclusive else alphaRange.start).clamp(alphaFRange))
    val colorEnd = Color(color.argb).alpha((if (!reverse) alphaRange.endInclusive else alphaRange.start).clamp(alphaFRange))
    renderGradientRect(matrixStack, rect, orientation, colorStart, colorEnd, shaderSupplier)
}

/**
 * 绘制饱和度和明度渐变矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle
 * @param orientation [Orientation]
 * @param reverse Boolean
 * @param saturationRange ClosedFloatingPointRange<Float>
 * @param valueRange ClosedFloatingPointRange<Float>
 * @param hue Float
 */
fun renderSVGradientRect(
    matrixStack: MatrixStack,
    rect: Box,
    orientation: Orientation = Orientation.Horizontal,
    reverse: Boolean = false,
    saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 360f,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram
) {
    val saturationStartColor = HSVColor(hue, if (reverse) saturationRange.endInclusive else saturationRange.start, valueRange.endInclusive)
    val saturationEndColor = HSVColor(hue, if (!reverse) saturationRange.endInclusive else saturationRange.start, valueRange.endInclusive)
    val alphaStartColor = Colors.BLACK.alpha((if (reverse) valueRange.endInclusive else valueRange.start).clamp(valueRange))
    val alphaEndColor = Colors.BLACK.alpha((if (!reverse) valueRange.endInclusive else valueRange.start).clamp(valueRange))
    rectBatchRender(shaderSupplier = shaderSupplier) {
        orientation.peek(
            {
                renderGradientRect(matrixStack, rect, orientation, saturationStartColor, saturationEndColor)
                renderGradientRect(matrixStack, rect, Orientation.Horizontal, alphaStartColor, alphaEndColor)
            }, {
                renderGradientRect(matrixStack, rect, orientation, saturationStartColor, saturationEndColor)
                renderGradientRect(matrixStack, rect, Orientation.Vertical, alphaStartColor, alphaEndColor)
            }
        )
    }

}

/**
 * 渲染矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle
 */
fun renderRect(matrixStack: MatrixStack, rect: ColoredBox, shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram) {
    enableBlend()
    setShader(shaderSupplier)
    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
    for (vertex in rect.coloredVertexes) {
        bufferBuilder.vertex(matrixStack, vertex).color(vertex.color).next()
    }
    bufferBuilder.draw()
    disableBlend()
}

/**
 * 渲染矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle
 * @param color ARGBColor
 */
fun renderRect(matrixStack: MatrixStack, rect: Box, color: ARGBColor, shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram) {
    enableBlend()
    setShader(shaderSupplier)
    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
    for (vertex in rect.vertexes) {
        bufferBuilder.vertex(matrixStack, vertex).color(color).next()
    }
    bufferBuilder.draw()
    disableBlend()
}

/**
 * 渲染矩形
 * @param matrixStack MatrixStack
 * @param coloredVertex Vertex
 * @param width Number
 * @param height Number
 */
fun renderRect(
    matrixStack: MatrixStack,
    coloredVertex: ColoredVertex,
    width: Number,
    height: Number,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram
) {
    renderRect(matrixStack, ColoredBox(coloredVertex, width.toFloat(), height.toFloat()), shaderSupplier)
}

/**
 * @see renderRect
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param color Color
 */
fun renderRect(matrixStack: MatrixStack, transform: Transform, color: ARGBColor, shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram) =
    renderRect(matrixStack, ColoredVertex(transform.worldPosition, color), transform.width, transform.height, shaderSupplier)

/**
 * Renders a round rectangle with the specified parameters.
 *
 * @param matrixStack The MatrixStack used for rendering.
 * @param rect The rectangle representing the dimensions and position of the round rectangle.
 * @param color The color of the round rectangle in ARGB format.
 * @param round The radius of the round corners of the rectangle.
 * @param pixelSize The size in pixels of each unit in the coordinate system. Default value is 1.0 pixel.
 */
fun renderRoundRect(
    matrixStack: MatrixStack,
    rect: Box,
    color: ARGBColor,
    round: Int,
    pixelSize: Float = 1f,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram
) {
    if (round > 0)
        rectBatchRender(shaderSupplier = shaderSupplier) {
            renderRect(matrixStack, Box(rect.position + Vector2f(0f, (round + 1) * pixelSize), rect.width, rect.height - ((round + 1) * pixelSize) * 2), color)
            renderRoundRectCache[RenderRoundRect(round, pixelSize, rect.width, rect.height)]?.let {
                it.forEach { (position, size) -> renderRect(matrixStack, Box(rect.position + position, size), color) }
                return@rectBatchRender
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
                            add(Vector2f(abs(x * pixelSize), abs(y!!.first) * pixelSize) to Size(rect.width - abs(x * pixelSize * 2), y.second * pixelSize))
                            add(
                                Vector2f(abs(x * pixelSize), rect.height - y.second * pixelSize - (abs(y.first)) * pixelSize)
                                        to Size(rect.width - abs(x * pixelSize * 2), y.second * pixelSize)
                            )
                        }
                renderRoundRectCache[RenderRoundRect(round, pixelSize, rect.width, rect.height)] = this
                if (size > renderRoundRectCacheSize) renderRoundRectCache.remove(renderRoundRectCache.keys.first())
            }.forEach { (position, size) -> renderRect(matrixStack, Box(rect.position + position, size), color) }
        }
    else renderRect(matrixStack, rect, color, shaderSupplier)
}

private data class RenderRoundRect(
    val round: Int,
    val pixelSize: Float,
    val width: Float,
    val height: Float
)

private const val renderRoundRectCacheSize = 50

private val renderRoundRectCache get() = mutableMapOf<RenderRoundRect, Set<Pair<Vector2fc, Size<Float>>>>()

/**
 * 渲染边框线条
 * @param matrixStack MatrixStack
 * @param coloredVertex Vertex
 * @param width Number
 * @param height Number
 * @param borderWidth Number
 */
fun renderOutline(
    matrixStack: MatrixStack,
    coloredVertex: ColoredVertex,
    width: Number,
    height: Number,
    borderWidth: Number = 1,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram
) {
    rectBatchRender(shaderSupplier = shaderSupplier) {
        renderRect(matrixStack, coloredVertex, borderWidth, height)
        renderRect(matrixStack, coloredVertex.copy(coloredVertex.x() + width.toFloat() - borderWidth.toFloat()), borderWidth, height)
        renderRect(matrixStack, coloredVertex.copy(coloredVertex.x() + borderWidth.toFloat()), width.toFloat() - 2 * borderWidth.toFloat(), borderWidth)
        renderRect(
            matrixStack, coloredVertex.copy(
                coloredVertex.x() + borderWidth.toFloat(),
                coloredVertex.y() + height.toFloat() - borderWidth.toFloat(),
            ),
            width.toFloat() - 2 * borderWidth.toFloat(), borderWidth
        )
    }
}

/**
 * @see renderOutline
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param color Color
 * @param borderWidth Number
 */
fun renderOutline(
    matrixStack: MatrixStack,
    transform: Transform,
    color: ARGBColor,
    borderWidth: Number = 1,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram
) =
    renderOutline(matrixStack, ColoredVertex(transform.worldPosition, color), transform.width, transform.height, borderWidth, shaderSupplier)

/**
 *  @see renderOutline
 * @param matrixStack MatrixStack
 * @param rect Rectangle
 * @param color Color
 * @param borderWidth Number
 */
fun renderOutline(
    matrixStack: MatrixStack,
    rect: Box,
    color: ARGBColor,
    borderWidth: Number = 1,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram
) =
    renderOutline(matrixStack, ColoredVertex(rect.position, color), rect.width, rect.height, borderWidth, shaderSupplier)


/**
 * 渲染带边框线条的矩形
 * @param matrixStack MatrixStack
 * @param coloredVertex Vertex
 * @param width Number
 * @param height Number
 * @param outlineColor Color
 * @param borderWidth Number
 * @param innerOutline Boolean
 */
fun renderOutlinedBox(
    matrixStack: MatrixStack,
    coloredVertex: ColoredVertex,
    width: Number,
    height: Number,
    outlineColor: ARGBColor,
    borderWidth: Number = 1,
    innerOutline: Boolean = true,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram
) {
    rectBatchRender(shaderSupplier = shaderSupplier) {
        if (innerOutline) {
            renderRect(matrixStack, coloredVertex, width, height)
            renderOutline(
                matrixStack,
                coloredVertex.copy(
                    x = coloredVertex.x() - borderWidth.toFloat(),
                    y = coloredVertex.y() - borderWidth.toFloat(),
                    color = outlineColor
                ),
                width.toFloat() + borderWidth.toFloat() * 2, height.toFloat() + borderWidth.toFloat() * 2,
                borderWidth
            )
        } else {
            renderRect(
                matrixStack,
                coloredVertex.copy(
                    coloredVertex.x() - borderWidth.toFloat(),
                    coloredVertex.y() - borderWidth.toFloat()
                ),
                width.toFloat() - 2 * borderWidth.toFloat(), height.toFloat() - 2 * borderWidth.toFloat(),
            )
            renderOutline(matrixStack, coloredVertex.copy(color = outlineColor), width, height, borderWidth)
        }
    }
}

/**
 * Renders an outlined box using the specified parameters.
 *
 * @param matrixStack The MatrixStack to apply transformations.
 * @param transform The Transform representing the position and size of the box.
 * @param color The color of the inner box in ARGB format.
 * @param outlineColor The color of the outline in ARGB format.
 * @param borderWidth The width of the outline.
 * @param innerOutline Indicates whether the outline is rendered inside the box.
 */
fun renderOutlinedBox(
    matrixStack: MatrixStack,
    transform: Transform,
    color: ARGBColor,
    outlineColor: ARGBColor,
    borderWidth: Number = 1,
    innerOutline: Boolean = true,
    shaderSupplier: () -> ShaderProgram? = GameRenderer::getPositionColorProgram
) {
    renderOutlinedBox(
        matrixStack, ColoredVertex(transform.worldPosition, color), transform.width, transform.height,
        outlineColor, borderWidth, innerOutline, shaderSupplier
    )
}
