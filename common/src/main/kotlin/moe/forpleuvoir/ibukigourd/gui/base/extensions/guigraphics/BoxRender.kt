@file:Suppress("DuplicatedCode", "unused")

package moe.forpleuvoir.ibukigourd.gui.base.extensions.guigraphics

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.ColoredBox
import moe.forpleuvoir.ibukigourd.render.IGRenderType
import moe.forpleuvoir.ibukigourd.render.color
import moe.forpleuvoir.ibukigourd.render.defaultZOffset
import moe.forpleuvoir.ibukigourd.render.vertex
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.common.color.alphaFRange
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import org.joml.Matrix4f
import org.joml.Vector2fc

/**
 * 渲染一个[Box]
 * @receiver IGGuiGraphics
 * @param box Box
 * @param color ARGBColor
 */
fun IGGuiGraphics.renderBox(
    box: Box,
    color: ARGBColor,
    renderType: RenderType = IGRenderType.GUI
) = renderBox(matrix4f, bufferSource, box, color, renderType)

fun renderBox(
    matrix4f: Matrix4f,
    bufferSource: MultiBufferSource.BufferSource,
    box: Box,
    color: ARGBColor,
    renderType: RenderType = IGRenderType.GUI
) {
    val bufferBuilder = bufferSource.getBuffer(renderType)
    for (vertex in box.vertexes) {
        bufferBuilder.vertex(matrix4f, vertex).color(color)
    }
    bufferSource.endBatch()
}

fun IGGuiGraphics.renderBox(
    coloredBox: ColoredBox,
    renderType: RenderType = IGRenderType.GUI
) = renderBox(matrix4f, bufferSource, coloredBox, renderType)

fun renderBox(
    matrix4f: Matrix4f,
    bufferSource: MultiBufferSource.BufferSource,
    coloredBox: ColoredBox,
    renderType: RenderType = IGRenderType.GUI
) {
    val bufferBuilder = bufferSource.getBuffer(renderType)
    for (vertex in coloredBox.coloredVertexes) {
        bufferBuilder.vertex(matrix4f, vertex).color(vertex.color)
    }
    bufferSource.endBatch()
}

fun IGGuiGraphics.renderBox(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: ARGBColor,
    renderType: RenderType = IGRenderType.GUI
) = renderBox(matrix4f, bufferSource, x, y, width, height, color, renderType)

fun renderBox(
    matrix4f: Matrix4f,
    bufferSource: MultiBufferSource.BufferSource,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: ARGBColor,
    renderType: RenderType = IGRenderType.GUI
) {
    val vertexConsumer = bufferSource.getBuffer(renderType)
    vertexConsumer.vertex(matrix4f, x = x, y = y, defaultZOffset).color(color)
    vertexConsumer.vertex(matrix4f, x = x, y = y + height, defaultZOffset).color(color)
    vertexConsumer.vertex(matrix4f, x = x + width, y = y + height, defaultZOffset).color(color)
    vertexConsumer.vertex(matrix4f, x = x + width, y = y, defaultZOffset).color(color)
    bufferSource.endBatch()
}

fun IGGuiGraphics.renderBox(
    position: Vector2fc,
    size: Size<Float>,
    color: ARGBColor,
    renderType: RenderType = IGRenderType.GUI,
) = renderBox(matrix4f, bufferSource, position, size, color, renderType)

fun renderBox(
    matrix4f: Matrix4f,
    bufferSource: MultiBufferSource.BufferSource,
    position: Vector2fc,
    size: Size<Float>,
    color: ARGBColor,
    renderType: RenderType = IGRenderType.GUI,
) = renderBox(matrix4f, bufferSource, position.x(), position.y(), size.width, size.height, color, renderType)


/**
 * 渲染一个[Box]
 */
fun IGGuiGraphics.renderBox(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    topLeftColor: ARGBColor,
    topRightColor: ARGBColor,
    bottomLeftColor: ARGBColor,
    bottomRightColor: ARGBColor,
    renderType: RenderType = IGRenderType.GUI
) = renderBox(matrix4f, bufferSource, x, y, width, height, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor, renderType)

fun renderBox(
    matrix4f: Matrix4f,
    bufferSource: MultiBufferSource.BufferSource,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    topLeftColor: ARGBColor,
    topRightColor: ARGBColor,
    bottomLeftColor: ARGBColor,
    bottomRightColor: ARGBColor,
    renderType: RenderType = IGRenderType.GUI
) {
    val vertexConsumer = bufferSource.getBuffer(renderType)
    vertexConsumer.vertex(matrix4f, x = x, y = y, defaultZOffset).color(topLeftColor)
    vertexConsumer.vertex(matrix4f, x = x, y = y + height, defaultZOffset).color(bottomLeftColor)
    vertexConsumer.vertex(matrix4f, x = x + width, y = y + height, defaultZOffset).color(bottomRightColor)
    vertexConsumer.vertex(matrix4f, x = x + width, y = y, defaultZOffset).color(topRightColor)
    bufferSource.endBatch()
}


fun IGGuiGraphics.renderGradientBox(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    startColor: ARGBColor,
    endColor: ARGBColor,
    orientation: Orientation = Orientation.Horizontal,
    renderType: RenderType = IGRenderType.GUI
) = renderGradientBox(matrix4f, bufferSource, x, y, width, height, startColor, endColor, orientation, renderType)

fun renderGradientBox(
    matrix4f: Matrix4f,
    bufferSource: MultiBufferSource.BufferSource,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    startColor: ARGBColor,
    endColor: ARGBColor,
    orientation: Orientation = Orientation.Horizontal,
    renderType: RenderType = IGRenderType.GUI
) {
    orientation.peek(
        {
            renderBox(
                matrix4f,
                bufferSource,
                x,
                y,
                width,
                height,
                topLeftColor = startColor,
                topRightColor = startColor,
                bottomLeftColor = endColor,
                bottomRightColor = endColor,
                renderType
            )
        },
        {
            renderBox(
                matrix4f,
                bufferSource,
                x,
                y,
                width,
                height,
                topLeftColor = startColor,
                topRightColor = endColor,
                bottomLeftColor = startColor,
                bottomRightColor = endColor,
                renderType
            )
        }
    )
}

fun IGGuiGraphics.renderGradientBox(
    box: Box,
    startColor: ARGBColor,
    endColor: ARGBColor,
    orientation: Orientation = Orientation.Horizontal,
    renderType: RenderType = IGRenderType.GUI
) = renderGradientBox(box.x, box.y, box.width, box.height, startColor, endColor, orientation, renderType)


/**
 * 渲染一个渐变[Box]
 * @receiver IGGuiGraphics
 * @param box Box
 * @param startColor ARGBColor
 * @param endColor ARGBColor
 * @param orientation Orientation
 */
fun IGGuiGraphics.renderGradientBox(
    matrix4f: Matrix4f,
    bufferSource: MultiBufferSource.BufferSource,
    box: Box,
    startColor: ARGBColor,
    endColor: ARGBColor,
    orientation: Orientation = Orientation.Horizontal,
    renderType: RenderType = IGRenderType.GUI
) = renderGradientBox(matrix4f, bufferSource, box.x, box.y, box.width, box.height, startColor, endColor, orientation, renderType)


/**
 * 渲染一个随饱和度渐变的[Box]
 * @receiver IGGuiGraphics
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
fun IGGuiGraphics.renderSaturationGradientBox(
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
    renderType: RenderType = IGRenderType.GUI
) = renderSaturationGradientBox(matrix4f, bufferSource, x, y, width, height, orientation, reverse, saturationRange, hue, value, alpha, renderType)

/**
 * 渲染一个随饱和度渐变的[Box]
 * @receiver IGGuiGraphics
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
fun renderSaturationGradientBox(
    matrix4f: Matrix4f,
    bufferSource: MultiBufferSource.BufferSource,
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
    renderType: RenderType = IGRenderType.GUI
) {
    check(saturationRange.endInclusive >= saturationRange.start) { "Saturation range must be in ascending order" }
    check(saturationRange.endInclusive in 0f..1f && saturationRange.start in 0f..1f) {
        "Saturation range must be between 0 and 1,but was ${saturationRange.start} and ${saturationRange.endInclusive}"
    }
    val colorStart = HSVColor(hue, (if (reverse) saturationRange.endInclusive else saturationRange.start).coerceIn(alphaFRange), value, alpha)
    val colorEnd = HSVColor(hue, (if (!reverse) saturationRange.endInclusive else saturationRange.start).coerceIn(alphaFRange), value, alpha)
    renderGradientBox(matrix4f, bufferSource, x, y, width, height, colorStart, colorEnd, orientation, renderType)
}

/**
 * 渲染一个随饱和度渐变的[Box]
 * @receiver IGGuiGraphics
 * @param box Box
 * @param orientation Orientation
 * @param reverse Boolean
 * @param saturationRange ClosedFloatingPointRange<Float>
 * @param hue Float
 * @param value Float
 * @param alpha Float
 */
fun IGGuiGraphics.renderSaturationGradientBox(
    box: Box,
    orientation: Orientation = Orientation.Horizontal,
    reverse: Boolean = false,
    saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 360f,
    value: Float = 1f,
    alpha: Float = 1f,
    renderType: RenderType = IGRenderType.GUI,
) = renderSaturationGradientBox(box.x, box.y, box.width, box.height, orientation, reverse, saturationRange, hue, value, alpha, renderType)


/**
 * 渲染一个随饱和度渐变的[Box]
 * @receiver IGGuiGraphics
 * @param box Box
 * @param orientation Orientation
 * @param reverse Boolean
 * @param saturationRange ClosedFloatingPointRange<Float>
 * @param hue Float
 * @param value Float
 * @param alpha Float
 */
fun renderSaturationGradientBox(
    matrix4f: Matrix4f,
    bufferSource: MultiBufferSource.BufferSource,
    box: Box,
    orientation: Orientation = Orientation.Horizontal,
    reverse: Boolean = false,
    saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 360f,
    value: Float = 1f,
    alpha: Float = 1f,
    renderType: RenderType = IGRenderType.GUI,
) = renderSaturationGradientBox(
    matrix4f,
    bufferSource,
    box.x,
    box.y,
    box.width,
    box.height,
    orientation,
    reverse,
    saturationRange,
    hue,
    value,
    alpha,
    renderType
)


/**
 * 渲染一个随明度渐变的[Box]
 * @receiver IGGuiGraphics
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
fun IGGuiGraphics.renderValueGradientBox(
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
    renderType: RenderType = IGRenderType.GUI,
) = renderValueGradientBox(matrix4f, bufferSource, x, y, width, height, orientation, reverse, valueRange, hue, saturation, alpha, renderType)

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
fun renderValueGradientBox(
    matrix4f: Matrix4f,
    bufferSource: MultiBufferSource.BufferSource,
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
    renderType: RenderType = IGRenderType.GUI,
) {
    check(valueRange.endInclusive >= valueRange.start) { "Value range must be in ascending order" }
    check(valueRange.endInclusive in 0f..1f && valueRange.start in 0f..1f) {
        "Value range must be between 0 and 1,but was ${valueRange.start} and ${valueRange.endInclusive}"
    }
    val colorStart = HSVColor(hue, saturation, (if (reverse) valueRange.endInclusive else valueRange.start).coerceIn(alphaFRange), alpha)
    val colorEnd = HSVColor(hue, saturation, (if (!reverse) valueRange.endInclusive else valueRange.start).coerceIn(alphaFRange), alpha)
    renderGradientBox(matrix4f, bufferSource, x, y, width, height, colorStart, colorEnd, orientation, renderType)
}

/**
 * 渲染一个随明度渐变的[Box]
 * @see [renderValueGradientBox]
 * @receiver IGGuiGraphics
 * @param box Box
 * @param orientation Orientation
 * @param reverse Boolean
 * @param valueRange ClosedFloatingPointRange<Float>
 * @param hue Float
 * @param saturation Float
 * @param alpha Float
 */
fun IGGuiGraphics.renderValueGradientBox(
    box: Box,
    orientation: Orientation = Orientation.Horizontal,
    reverse: Boolean = false,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 360f,
    saturation: Float = 1f,
    alpha: Float = 1f,
    renderType: RenderType = IGRenderType.GUI,
) = renderValueGradientBox(box.x, box.y, box.width, box.height, orientation, reverse, valueRange, hue, saturation, alpha, renderType)

/**
 * 渲染一个随明度渐变的[Box]
 * @see [renderValueGradientBox]
 * @receiver IGGuiGraphics
 * @param box Box
 * @param orientation Orientation
 * @param reverse Boolean
 * @param valueRange ClosedFloatingPointRange<Float>
 * @param hue Float
 * @param saturation Float
 * @param alpha Float
 */
fun renderValueGradientBox(
    matrix4f: Matrix4f,
    bufferSource: MultiBufferSource.BufferSource,
    box: Box,
    orientation: Orientation = Orientation.Horizontal,
    reverse: Boolean = false,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 360f,
    saturation: Float = 1f,
    alpha: Float = 1f,
    renderType: RenderType = IGRenderType.GUI,
) = renderValueGradientBox(matrix4f, bufferSource, box.x, box.y, box.width, box.height, orientation, reverse, valueRange, hue, saturation, alpha, renderType)

