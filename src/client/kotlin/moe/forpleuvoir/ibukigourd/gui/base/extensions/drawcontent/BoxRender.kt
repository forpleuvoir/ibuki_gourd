@file:Suppress("DuplicatedCode", "unused")

package moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.peek
import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.Box
import moe.forpleuvoir.ibukigourd.gui.base.render.shape.box.ColoredBox
import moe.forpleuvoir.ibukigourd.render.color
import moe.forpleuvoir.ibukigourd.render.setShader
import moe.forpleuvoir.ibukigourd.render.vertex
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.HSVColor
import moe.forpleuvoir.nebula.common.color.alphaFRange
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.RenderLayer
import org.joml.Vector2fc

/**
 * 渲染一个[Box]
 * @receiver DrawContext
 * @param box Box
 * @param color ARGBColor
 * @param shaderSupplier () -> ShaderProgram?
 */
fun DrawContext.renderBox(
    box: Box,
    color: ARGBColor,
    layer: RenderLayer = RenderLayer.getGui(),
    shaderSupplier: (() -> ShaderProgram?)? = GameRenderer::getPositionColorProgram,
) {
    setShader(shaderSupplier)
    val bufferBuilder = vertexConsumers.getBuffer(layer)
    for (vertex in box.vertexes) {
        bufferBuilder.vertex(matrices, vertex).color(color)
    }
    draw()
}

/**
 * 渲染一个[ColoredBox]
 * @receiver DrawContext
 * @param coloredBox ColoredBox
 * @param shaderSupplier () -> ShaderProgram?
 */
fun DrawContext.renderBox(
    coloredBox: ColoredBox,
    layer: RenderLayer = RenderLayer.getGui(),
    shaderSupplier: (() -> ShaderProgram?)? = GameRenderer::getPositionColorProgram,
) {
    setShader(shaderSupplier)
    val bufferBuilder = vertexConsumers.getBuffer(layer)
    for (vertex in coloredBox.coloredVertexes) {
        bufferBuilder.vertex(matrices, vertex).color(vertex.color)
    }
    draw()
}


/**
 * 渲染一个[Box]
 * @receiver DrawContext
 * @param x Number
 * @param y Number
 * @param width Number
 * @param height Number
 * @param color ARGBColor
 */
fun DrawContext.renderBox(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: ARGBColor,
    layer: RenderLayer = RenderLayer.getGui(),
    shaderSupplier: (() -> ShaderProgram?)? = GameRenderer::getPositionColorProgram,
) {
    setShader(shaderSupplier)
    val bufferBuilder = vertexConsumers.getBuffer(layer)
    bufferBuilder.vertex(matrices, x = x, y = y, 0f).color(color)
    bufferBuilder.vertex(matrices, x = x + width, y = y, 0f).color(color)
    bufferBuilder.vertex(matrices, x = x + width, y = y + height, 0f).color(color)
    bufferBuilder.vertex(matrices, x = x, y = y + height, 0f).color(color)
    draw()
}

/**
 * 渲染一个[Box]
 * @receiver DrawContext
 * @param position Vector2fc
 * @param size Size<Float>
 * @param color ARGBColor
 */
fun DrawContext.renderBox(
    position: Vector2fc,
    size: Size<Float>,
    color: ARGBColor,
    layer: RenderLayer = RenderLayer.getGui(),
    shaderSupplier: (() -> ShaderProgram?)? = GameRenderer::getPositionColorProgram,
) {
    renderBox(position.x(), position.y(), size.width, size.height, color, layer, shaderSupplier)
}

/**
 * 渲染一个[Box]
 * @receiver DrawContext
 * @param x Float
 * @param y Float
 * @param width Float
 * @param height Float
 * @param topLeftColor ARGBColor
 * @param topRightColor ARGBColor
 * @param bottomLeftColor ARGBColor
 * @param bottomRightColor ARGBColor
 * @param shaderSupplier () -> ShaderProgram?
 */
fun DrawContext.renderBox(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    topLeftColor: ARGBColor,
    topRightColor: ARGBColor,
    bottomLeftColor: ARGBColor,
    bottomRightColor: ARGBColor,
    layer: RenderLayer = RenderLayer.getGui(),
    shaderSupplier: (() -> ShaderProgram?)? = GameRenderer::getPositionColorProgram,
) {
    setShader(shaderSupplier)
    val bufferBuilder = vertexConsumers.getBuffer(layer)
    bufferBuilder.vertex(matrices, x = x, y = y, 0f).color(topLeftColor)
    bufferBuilder.vertex(matrices, x = x + width, y = y, 0f).color(topRightColor)
    bufferBuilder.vertex(matrices, x = x + width, y = y + height, 0f).color(bottomRightColor)
    bufferBuilder.vertex(matrices, x = x, y = y + height, 0f).color(bottomLeftColor)
    draw()
}

/**
 * 渲染一个渐变[Box]
 * @receiver DrawContext
 * @param x Float
 * @param y Float
 * @param width Float
 * @param height Float
 * @param startColor ARGBColor
 * @param endColor ARGBColor
 * @param orientation Orientation
 * @param shaderSupplier () -> ShaderProgram?
 */
fun DrawContext.renderGradientBox(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    startColor: ARGBColor,
    endColor: ARGBColor,
    orientation: Orientation = Orientation.Horizontal,
    layer: RenderLayer = RenderLayer.getGui(),
    shaderSupplier: (() -> ShaderProgram?)? = GameRenderer::getPositionColorProgram,
) {
    orientation.peek(
        renderBox(
            x,
            y,
            width,
            height,
            topLeftColor = startColor,
            topRightColor = startColor,
            bottomLeftColor = endColor,
            bottomRightColor = endColor,
            layer,
            shaderSupplier
        ),
        renderBox(
            x,
            y,
            width,
            height,
            topLeftColor = startColor,
            topRightColor = endColor,
            bottomLeftColor = startColor,
            bottomRightColor = endColor,
            layer,
            shaderSupplier
        )
    )
}

/**
 * 渲染一个渐变[Box]
 * @receiver DrawContext
 * @param box Box
 * @param startColor ARGBColor
 * @param endColor ARGBColor
 * @param orientation Orientation
 * @param shaderSupplier () -> ShaderProgram?
 */
fun DrawContext.renderGradientBox(
    box: Box,
    startColor: ARGBColor,
    endColor: ARGBColor,
    orientation: Orientation = Orientation.Horizontal,
    layer: RenderLayer = RenderLayer.getGui(),
    shaderSupplier: (() -> ShaderProgram?)? = GameRenderer::getPositionColorProgram,
) {
    renderGradientBox(box.x, box.y, box.width, box.height, startColor, endColor, orientation, layer, shaderSupplier)
}

/**
 * 渲染一个随饱和度渐变的[Box]
 * @receiver DrawContext
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
 * @param shaderSupplier () -> ShaderProgram?
 */
fun DrawContext.renderSaturationGradientBox(
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
    layer: RenderLayer = RenderLayer.getGui(),
    shaderSupplier: (() -> ShaderProgram?)? = GameRenderer::getPositionColorProgram,
) {
    check(saturationRange.endInclusive >= saturationRange.start) { "Saturation range must be in ascending order" }
    check(saturationRange.endInclusive in 0f..1f && saturationRange.start in 0f..1f) {
        "Saturation range must be between 0 and 1,but was ${saturationRange.start} and ${saturationRange.endInclusive}"
    }
    val colorStart = HSVColor(hue, (if (reverse) saturationRange.endInclusive else saturationRange.start).coerceIn(alphaFRange), value, alpha)
    val colorEnd = HSVColor(hue, (if (!reverse) saturationRange.endInclusive else saturationRange.start).coerceIn(alphaFRange), value, alpha)
    renderGradientBox(x, y, width, height, colorStart, colorEnd, orientation, layer, shaderSupplier)
}

/**
 * 渲染一个随饱和度渐变的[Box]
 * @receiver DrawContext
 * @param box Box
 * @param orientation Orientation
 * @param reverse Boolean
 * @param saturationRange ClosedFloatingPointRange<Float>
 * @param hue Float
 * @param value Float
 * @param alpha Float
 * @param shaderSupplier () -> ShaderProgram?
 */
fun DrawContext.renderSaturationGradientBox(
    box: Box,
    orientation: Orientation = Orientation.Horizontal,
    reverse: Boolean = false,
    saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 360f,
    value: Float = 1f,
    alpha: Float = 1f,
    layer: RenderLayer = RenderLayer.getGui(),
    shaderSupplier: (() -> ShaderProgram?)? = GameRenderer::getPositionColorProgram,
) {
    renderSaturationGradientBox(box.x, box.y, box.width, box.height, orientation, reverse, saturationRange, hue, value, alpha, layer, shaderSupplier)
}

/**
 * 渲染一个随明度渐变的[Box]
 * @receiver DrawContext
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
 * @param shaderSupplier () -> ShaderProgram?
 */
fun DrawContext.renderValueGradientBox(
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
    layer: RenderLayer = RenderLayer.getGui(),
    shaderSupplier: (() -> ShaderProgram?)? = GameRenderer::getPositionColorProgram,
) {
    check(valueRange.endInclusive >= valueRange.start) { "Value range must be in ascending order" }
    check(valueRange.endInclusive in 0f..1f && valueRange.start in 0f..1f) {
        "Value range must be between 0 and 1,but was ${valueRange.start} and ${valueRange.endInclusive}"
    }
    val colorStart = HSVColor(hue, saturation, (if (reverse) valueRange.endInclusive else valueRange.start).coerceIn(alphaFRange), alpha)
    val colorEnd = HSVColor(hue, saturation, (if (!reverse) valueRange.endInclusive else valueRange.start).coerceIn(alphaFRange), alpha)
    renderGradientBox(x, y, width, height, colorStart, colorEnd, orientation, layer, shaderSupplier)
}

/**
 * 渲染一个随明度渐变的[Box]
 * @see [renderValueGradientBox]
 * @receiver DrawContext
 * @param box Box
 * @param orientation Orientation
 * @param reverse Boolean
 * @param valueRange ClosedFloatingPointRange<Float>
 * @param hue Float
 * @param saturation Float
 * @param alpha Float
 * @param shaderSupplier () -> ShaderProgram?
 */
fun DrawContext.renderValueGradientBox(
    box: Box,
    orientation: Orientation = Orientation.Horizontal,
    reverse: Boolean = false,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    hue: Float = 360f,
    saturation: Float = 1f,
    alpha: Float = 1f,
    layer: RenderLayer = RenderLayer.getGui(),
    shaderSupplier: (() -> ShaderProgram?)? = GameRenderer::getPositionColorProgram,
) {
    renderValueGradientBox(box.x, box.y, box.width, box.height, orientation, reverse, valueRange, hue, saturation, alpha,layer, shaderSupplier)
}
