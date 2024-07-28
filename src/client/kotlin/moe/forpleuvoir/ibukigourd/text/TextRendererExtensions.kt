package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.nebula.common.color.ARGBColor
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.font.TextRenderer.TextLayerType
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.text.OrderedText
import org.joml.Matrix4f

fun TextRenderer.draw(
    text: OrderedText,
    x: Float,
    y: Float,
    color: ARGBColor,
    shadow: Boolean,
    matrix: Matrix4f,
    vertexConsumers: VertexConsumerProvider,
    layerType: TextLayerType,
    backgroundColor: ARGBColor,
    light: Int
): Int = this.drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light)


fun TextRenderer.drawInternal(
    text: OrderedText,
    x: Float,
    y: Float,
    color: ARGBColor,
    shadow: Boolean,
    matrix: Matrix4f,
    vertexConsumerProvider: VertexConsumerProvider,
    layerType: TextLayerType,
    backgroundColor: ARGBColor,
    light: Int
): Int {
    var xPos = x
    val matrix4f = Matrix4f(matrix)
    if (shadow) {
        this.drawLayer(text, xPos, y, color.argb, true, matrix, vertexConsumerProvider, layerType, backgroundColor.argb, light)
        matrix4f.translate(0f, 0f, 0.03f)
    }

    xPos = this.drawLayer(text, xPos, y, color.argb, false, matrix4f, vertexConsumerProvider, layerType, backgroundColor.argb, light)
    return xPos.toInt() + (if (shadow) 1 else 0)
}
