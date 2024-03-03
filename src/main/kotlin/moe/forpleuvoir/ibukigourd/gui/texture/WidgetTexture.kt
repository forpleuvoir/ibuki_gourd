package moe.forpleuvoir.ibukigourd.gui.texture

import moe.forpleuvoir.ibukigourd.render.base.texture.Corner
import moe.forpleuvoir.ibukigourd.render.base.texture.TextureInfo
import moe.forpleuvoir.ibukigourd.render.base.texture.TextureUVMapping
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType

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

    companion object {
        fun deserialization(serializeElement: SerializeElement?, default: WidgetTexture): WidgetTexture {
            return serializeElement?.run {
                checkType<WidgetTexture>()
                    .check<SerializeObject> {
                        WidgetTexture(
                            textureUVMapping = TextureUVMapping.deserialization(this, default),
                            textureInfo = TextureInfo.deserialization(it["texture_info"], default.textureInfo)
                        )
                    }.getOrDefault(default)
            } ?: default
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

        return textureInfo == other.textureInfo
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + textureInfo.hashCode()
        return result
    }


}