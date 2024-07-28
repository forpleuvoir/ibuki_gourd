package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.nebula.common.color.ARGBColor
import net.minecraft.client.font.TextRenderer.TextLayerType
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.VertexConsumerProvider
import org.joml.Matrix4f

class TextDrawer(
    val color: ARGBColor,
    val matrix: Matrix4f,
    val vertexConsumers: VertexConsumerProvider,
    val layerType: TextLayerType = TextLayerType.NORMAL,
    val light: Int = LightmapTextureManager.MAX_LIGHT_COORDINATE
) {

    companion object {

        fun of(text: Text) {

        }

    }

    fun draw() {}

}