package moe.forpleuvoir.ibukigourd.gui.base.render.texture

import moe.forpleuvoir.ibukigourd.render.asTexture
import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import net.minecraft.client.gui.render.TextureSetup

class WidgetTexture(
    corner: Corner,
    u1: Int, v1: Int, u2: Int, v2: Int,
    val textureInfo: TextureInfo
) : TextureUVMapping(corner, u1, v1, u2, v2) {

    constructor(textureUVMapping: TextureUVMapping, textureInfo: TextureInfo) : this(
        textureUVMapping.corner,
        textureUVMapping.uStart,
        textureUVMapping.vStart,
        textureUVMapping.uEnd,
        textureUVMapping.vEnd,
        textureInfo
    )

    companion object : Deserializer<WidgetTexture> {

        override fun deserialization(serializeElement: SerializeElement): WidgetTexture {
            return serializeElement.checkType<WidgetTexture>()
                .check<SerializeObject> {
                    WidgetTexture(
                        textureUVMapping = TextureUVMapping.deserialization(it),
                        textureInfo = TextureInfo.deserialization(it["texture_info"]!!)
                    )
                }.getOrThrow()
        }

    }

    val textureSetup: TextureSetup by lazy {
        textureInfo.texture.asTexture.let {
            TextureSetup.singleTexture(it.textureView, it.sampler)
        }
    }

    val u0 = uStart.toFloat() / textureInfo.width

    val v0 = vStart.toFloat() / textureInfo.height

    val u1 = uEnd.toFloat() / textureInfo.width

    val v1 = vEnd.toFloat() / textureInfo.height

    override fun serialization(): SerializeElement {
        return super.serialization().asObject.apply {
            this["texture_info"] = textureInfo.serialization()
        }
    }


    override fun toString(): String {
        return "WidgetTexture(corner=$corner,uStart=$uStart, vStart=$vStart, uEnd=$uEnd, vEnd=$vEnd,textureInfo=$textureInfo)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as WidgetTexture

        return textureInfo == other.textureInfo && super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + textureInfo.hashCode()
        return result
    }


}