package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.util.primitive.pick
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
    light: Int,
    mirror: Boolean
): Int = this.drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light, mirror)


fun TextRenderer.drawInternal(
    text: OrderedText,
    x: Float,
    y: Float,
    color: ARGBColor,
    shadow: Boolean,
    matrix: Matrix4f,
    vertexConsumers: VertexConsumerProvider,
    layerType: TextLayerType,
    backgroundColor: ARGBColor,
    light: Int,
    mirror: Boolean
): Int {
    var xPos = x
    xPos = this.drawLayer(text, x, y, color.argb, shadow, matrix, vertexConsumers, layerType, backgroundColor.argb, light, mirror)
    return xPos.toInt() + shadow.pick(1, 0)
}
