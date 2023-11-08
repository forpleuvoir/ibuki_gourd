package moe.forpleuvoir.ibukigourd.render.helper

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.render.base.Arrangement
import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.rectangle.Rectangle
import moe.forpleuvoir.ibukigourd.render.base.rectangle.colorRect
import moe.forpleuvoir.ibukigourd.render.base.vertex.ColorVertex
import moe.forpleuvoir.ibukigourd.render.base.vertex.ColorVertexImpl
import moe.forpleuvoir.ibukigourd.render.base.vertex.colorVertex
import moe.forpleuvoir.nebula.common.color.*
import moe.forpleuvoir.nebula.common.util.clamp
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f

/**
 * 渲染线段
 * @param matrixStack MatrixStack
 * @param lineWidth Number
 * @param vertex1 ColorVertex
 * @param vertex2 ColorVertex
 */
fun renderLine(matrixStack: MatrixStack, lineWidth: Number, vertex1: ColorVertex, vertex2: ColorVertex, normal: Vector3<Float>) {
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
 * @param vertexes [List]<[ColorVertex]>
 */
fun renderLine(matrixStack: MatrixStack, lineWidth: Number, vararg vertexes: ColorVertex) {
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
 * @param matrix4f Matrix4f
 * @param shaderSupplier () -> ShaderProgram?
 * @param drawMode VertexFormat.DrawMode
 * @param format VertexFormat
 * @param bufferBuilder BufferBuilder
 * @param drawAction BatchDrawScope.() -> Unit
 */
fun rectBatchDraw(
    matrixStack: MatrixStack,
    bufferBuilder: BufferBuilder = moe.forpleuvoir.ibukigourd.render.helper.bufferBuilder,
    drawAction: RectBatchDrawScope.() -> Unit
) {
    enableBlend()
    setShader(GameRenderer::getPositionColorProgram)
    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
    RectBatchDrawScope.bufferBuilder = bufferBuilder
    RectBatchDrawScope.matrixStack = matrixStack
    drawAction(RectBatchDrawScope)
    bufferBuilder.draw()
    disableBlend()
    RectBatchDrawScope.bufferBuilder = null
    RectBatchDrawScope.matrixStack = null
}

object RectBatchDrawScope {

    internal var bufferBuilder: BufferBuilder? = null

    internal var matrixStack: MatrixStack? = null

    /**
     * 渲染矩形
     * @param matrixStack MatrixStack
     * @param rect Rectangle
     */
    fun renderRect(rect: Rectangle<ColorVertex>) {
        for (vertex in rect.vertexes) {
            bufferBuilder!!.vertex(matrixStack!!, vertex).color(vertex.color).next()
        }
    }

    /**
     * 渲染矩形
     * @param matrixStack MatrixStack
     * @param rect Rectangle<Vector3<Float>>
     * @param color ARGBColor
     */
    fun renderRect(rect: Rectangle<Vector3<Float>>, color: ARGBColor) {
        for (vertex in rect.vertexes) {
            bufferBuilder!!.vertex(matrixStack!!, vertex).color(color).next()
        }
    }

    /**
     * 渲染矩形
     * @param matrixStack MatrixStack
     * @param colorVertex Vertex
     * @param width Number
     * @param height Number
     */
    fun renderRect(colorVertex: ColorVertex, width: Number, height: Number) {
        renderRect(colorRect(colorVertex, width.toFloat(), height.toFloat()))
    }

    /**
     * @see renderRect
     * @param matrixStack MatrixStack
     * @param transform Transform
     * @param color Color
     */
    fun renderRect(transform: Transform, color: ARGBColor) =
        renderRect(colorVertex(transform.worldPosition, color), transform.width, transform.height)

}

fun renderGradientRect(
    matrixStack: MatrixStack,
    rect: Rectangle<Vector3<Float>>,
    arrangement: Arrangement = Arrangement.Horizontal,
    startColor: ARGBColor,
    endColor: ARGBColor
) {
    arrangement.switch(
        {
            renderRect(matrixStack, colorRect(rect, startColor, endColor, endColor, startColor))
        }, {
            renderRect(matrixStack, colorRect(rect, startColor, startColor, endColor, endColor))
        }
    )
}

/**
 * 渲染渐变矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle<Vector3<Float>>
 * @param precision Int 精度
 * @param arrangement Arrangement
 * @param reverse Boolean
 * @param hueRange ClosedFloatingPointRange<Float>
 * @param saturation Float
 * @param value Float
 * @param alpha Float
 */
fun renderHueGradientRect(
    matrixStack: MatrixStack,
    rect: Rectangle<Vector3<Float>>,
    precision: Int,
    arrangement: Arrangement = Arrangement.Horizontal,
    reverse: Boolean = false,
    hueRange: ClosedFloatingPointRange<Float> = 0f..360f,
    saturation: Float = 1f,
    value: Float = 1f,
    alpha: Float = 1f
) {
    val hueSlice = hueRange.endInclusive / precision
    var hue = if (reverse) hueRange.endInclusive else hueRange.start
    rectBatchDraw(matrixStack) {
        arrangement.switch(
            {
                val lengthSlice = rect.height / precision
                var y = rect.y
                for (i in 0 until precision) {
                    val colorStart = HSVColor(hue, saturation, value, alpha, false)
                    hue = if (reverse) (hue - hueSlice).clamp(hueRange) else (hue + hueSlice).clamp(hueRange)
                    val colorEnd = HSVColor(hue, saturation, value, alpha, false)
                    renderRect(colorRect(rect.x, y, rect.z, Size.create(rect.width, lengthSlice), colorStart, colorEnd, colorEnd, colorStart))
                    y += lengthSlice
                }

            }, {
                val lengthSlice = rect.width / precision
                var x = rect.x
                for (i in 0 until precision) {
                    val colorStart = HSVColor(hue, saturation, value, alpha, false)
                    hue = if (reverse) (hue - hueSlice).clamp(hueRange) else (hue + hueSlice).clamp(hueRange)
                    val colorEnd = HSVColor(hue, saturation, value, alpha, false)
                    renderRect(colorRect(x, rect.y, rect.z, Size.create(lengthSlice, rect.height), colorStart, colorStart, colorEnd, colorEnd))
                    x += lengthSlice
                }
            }
        )
    }
}

/**
 * 绘制饱和度渐变矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle<Vector3<Float>>
 * @param arrangement Arrangement
 * @param reverse Boolean
 * @param saturationRange ClosedFloatingPointRange<Float>
 * @param hue Float
 * @param value Float
 * @param alpha Float
 */
fun renderSaturationGradientRect(
    matrixStack: MatrixStack,
    rect: Rectangle<Vector3<Float>>,
    arrangement: Arrangement = Arrangement.Horizontal,
    reverse: Boolean = false,
    saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 360f,
    value: Float = 1f,
    alpha: Float = 1f
) {
    val colorStart = HSVColor(hue, (if (reverse) saturationRange.endInclusive else saturationRange.start).clamp(alphaFRange), value, alpha)
    val colorEnd = HSVColor(hue, (if (!reverse) saturationRange.endInclusive else saturationRange.start).clamp(alphaFRange), value, alpha)
    renderGradientRect(matrixStack, rect, arrangement, colorStart, colorEnd)
}

/**
 * 绘制饱和度渐变矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle<Vector3<Float>>
 * @param arrangement Arrangement
 * @param reverse Boolean
 * @param valueRange ClosedFloatingPointRange<Float>
 * @param hue Float
 * @param saturation Float
 * @param alpha Float
 */
fun renderValueGradientRect(
    matrixStack: MatrixStack,
    rect: Rectangle<Vector3<Float>>,
    arrangement: Arrangement = Arrangement.Horizontal,
    reverse: Boolean = false,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 360f,
    saturation: Float = 1f,
    alpha: Float = 1f
) {
    val colorStart = HSVColor(hue, saturation, (if (reverse) valueRange.endInclusive else valueRange.start).clamp(alphaFRange), alpha)
    val colorEnd = HSVColor(hue, saturation, (if (!reverse) valueRange.endInclusive else valueRange.start).clamp(alphaFRange), alpha)
    renderGradientRect(matrixStack, rect, arrangement, colorStart, colorEnd)
}

/**
 * 绘制透明度渐变矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle<Vector3<Float>>
 * @param arrangement Arrangement
 * @param reverse Boolean
 * @param alphaRange ClosedFloatingPointRange<Float>
 * @param color ARGBColor
 */
fun renderAlphaGradientRect(
    matrixStack: MatrixStack,
    rect: Rectangle<Vector3<Float>>,
    arrangement: Arrangement = Arrangement.Horizontal,
    reverse: Boolean = false,
    alphaRange: ClosedFloatingPointRange<Float> = 0f..1f,
    color: ARGBColor
) {
    val colorStart = Color(color.argb).alpha((if (reverse) alphaRange.endInclusive else alphaRange.start).clamp(alphaFRange))
    val colorEnd = Color(color.argb).alpha((if (!reverse) alphaRange.endInclusive else alphaRange.start).clamp(alphaFRange))
    renderGradientRect(matrixStack, rect, arrangement, colorStart, colorEnd)
}

/**
 * 绘制饱和度和明度渐变矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle<Vector3<Float>>
 * @param arrangement Arrangement
 * @param reverse Boolean
 * @param saturationRange ClosedFloatingPointRange<Float>
 * @param valueRange ClosedFloatingPointRange<Float>
 * @param hue Float
 */
fun renderSVGradientRect(
    matrixStack: MatrixStack,
    rect: Rectangle<Vector3<Float>>,
    arrangement: Arrangement = Arrangement.Horizontal,
    reverse: Boolean = false,
    saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 360f
) {
    val saturationStart = saturationRange.start
    val saturationEnd = saturationRange.endInclusive
    val valueStart = valueRange.start
    val valueEnd = valueRange.endInclusive
    val saturationStartColor = HSVColor(hue, if (reverse) saturationEnd else saturationStart, valueEnd)
    val saturationEndColor = HSVColor(hue, if (!reverse) saturationEnd else saturationStart, valueEnd)
    val alphaStartColor = Colors.BLACK.alpha((if (reverse) valueEnd else valueStart).clamp(valueRange))
    val alphaEndColor = Colors.BLACK.alpha((if (!reverse) valueEnd else valueStart).clamp(valueRange))
    arrangement.switch(
        {
            renderGradientRect(matrixStack, rect, arrangement, saturationStartColor, saturationEndColor)
            renderGradientRect(matrixStack, rect, Arrangement.Horizontal, alphaStartColor, alphaEndColor)
        }, {
            renderGradientRect(matrixStack, rect, arrangement, saturationStartColor, saturationEndColor)
            renderGradientRect(matrixStack, rect, Arrangement.Vertical, alphaStartColor, alphaEndColor)
        }
    )
}

/**
 * 渲染矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle
 */
fun renderRect(matrixStack: MatrixStack, rect: Rectangle<ColorVertex>) {
    enableBlend()
    setShader(GameRenderer::getPositionColorProgram)
    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
    for (vertex in rect.vertexes) {
        bufferBuilder.vertex(matrixStack, vertex).color(vertex.color).next()
    }
    bufferBuilder.draw()
    disableBlend()
}

/**
 * 渲染矩形
 * @param matrixStack MatrixStack
 * @param rect Rectangle<Vector3<Float>>
 * @param color ARGBColor
 */
fun renderRect(matrixStack: MatrixStack, rect: Rectangle<Vector3<Float>>, color: ARGBColor) {
    enableBlend()
    setShader(GameRenderer::getPositionColorProgram)
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
 * @param colorVertex Vertex
 * @param width Number
 * @param height Number
 */
fun renderRect(matrixStack: MatrixStack, colorVertex: ColorVertex, width: Number, height: Number) {
    renderRect(matrixStack, colorRect(colorVertex, width.toFloat(), height.toFloat()))
}

/**
 * @see renderRect
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param color Color
 */
fun renderRect(matrixStack: MatrixStack, transform: Transform, color: ARGBColor) =
    renderRect(matrixStack, colorVertex(transform.worldPosition, color), transform.width, transform.height)


/**
 * 渲染边框线条
 * @param matrixStack MatrixStack
 * @param colorVertex Vertex
 * @param width Number
 * @param height Number
 * @param borderWidth Number
 */
fun renderOutline(matrixStack: MatrixStack, colorVertex: ColorVertex, width: Number, height: Number, borderWidth: Number = 1) {
    renderRect(matrixStack, colorVertex, borderWidth, height)
    renderRect(matrixStack, colorVertex.x(colorVertex.x + width.toFloat() - borderWidth.toFloat()), borderWidth, height)
    renderRect(matrixStack, colorVertex.x(colorVertex.x + borderWidth.toFloat()), width.toFloat() - 2 * borderWidth.toFloat(), borderWidth)
    renderRect(
        matrixStack, colorVertex.xyz(
            colorVertex.x + borderWidth.toFloat(),
            colorVertex.y + height.toFloat() - borderWidth.toFloat(),
        ),
        width.toFloat() - 2 * borderWidth.toFloat(), borderWidth
    )
}

/**
 * @see renderOutline
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param color Color
 * @param borderWidth Number
 */
fun renderOutline(matrixStack: MatrixStack, transform: Transform, color: ARGBColor, borderWidth: Number = 1) =
    renderOutline(matrixStack, ColorVertexImpl(transform.worldPosition, color), transform.width, transform.height, borderWidth)

/**
 *  @see renderOutline
 * @param matrixStack MatrixStack
 * @param rect Rectangle<Vector3<Float>>
 * @param color Color
 * @param borderWidth Number
 */
fun renderOutline(matrixStack: MatrixStack, rect: Rectangle<Vector3<Float>>, color: ARGBColor, borderWidth: Number = 1) =
    renderOutline(matrixStack, ColorVertexImpl(rect.position, color), rect.width, rect.height, borderWidth)


/**
 * 渲染带边框线条的矩形
 * @param matrixStack MatrixStack
 * @param colorVertex Vertex
 * @param width Number
 * @param height Number
 * @param outlineColor Color
 * @param borderWidth Number
 * @param innerOutline Boolean
 */
fun renderOutlinedBox(
    matrixStack: MatrixStack,
    colorVertex: ColorVertex,
    width: Number,
    height: Number,
    outlineColor: ARGBColor,
    borderWidth: Number = 1,
    innerOutline: Boolean = true,
) {
    if (innerOutline) {
        renderRect(matrixStack, colorVertex, width, height)
        renderOutline(
            matrixStack,
            colorVertex.xyz(
                colorVertex.x - borderWidth.toFloat(),
                colorVertex.y - borderWidth.toFloat()
            ).color(outlineColor),
            width.toFloat() + borderWidth.toFloat() * 2, height.toFloat() + borderWidth.toFloat() * 2,
            borderWidth
        )
    } else {
        renderRect(
            matrixStack,
            colorVertex.xyz(
                colorVertex.x - borderWidth.toFloat(),
                colorVertex.y - borderWidth.toFloat()
            ),
            width.toFloat() - 2 * borderWidth.toFloat(), height.toFloat() - 2 * borderWidth.toFloat(),
        )
        renderOutline(matrixStack, colorVertex.color(outlineColor), width, height, borderWidth)
    }
}

/**
 * @see renderOutlinedBox
 * @param matrixStack MatrixStack
 * @param transform Transform
 * @param color Color
 * @param outlineColor Color
 * @param borderWidth Number
 * @param innerOutline Boolean
 */
fun renderOutlinedBox(
    matrixStack: MatrixStack,
    transform: Transform,
    color: ARGBColor,
    outlineColor: ARGBColor,
    borderWidth: Number = 1,
    innerOutline: Boolean = true
) {
    renderOutlinedBox(
        matrixStack, ColorVertexImpl(transform.worldPosition, color), transform.width, transform.height,
        outlineColor, borderWidth, innerOutline,
    )
}