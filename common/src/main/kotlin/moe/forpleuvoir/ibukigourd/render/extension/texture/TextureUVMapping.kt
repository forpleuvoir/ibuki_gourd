package moe.forpleuvoir.ibukigourd.render.extension.texture

import moe.forpleuvoir.nebula.common.util.checkType
import moe.forpleuvoir.nebula.common.util.requireKey
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.codec.Codec

open class TextureUVMapping(
    val corner: Corner,
    u0: Int, v0: Int, u1: Int, v1: Int
) : UVMapping(u0, v0, u1, v1) {

    constructor(corner: Corner, uvMapping: UVMapping) : this(
        corner,
        uvMapping.uStart, uvMapping.vStart, uvMapping.uEnd, uvMapping.vEnd
    )

    companion object : Codec<TextureUVMapping> {
        override fun serialization(target: TextureUVMapping): SerializeElement =
            UVMapping.serialization(target).asObject!!.apply { this["corner"] = Corner.serialization(target.corner) }

        override fun deserialization(data: SerializeElement): Result<TextureUVMapping> = DeserializationException.runCatching {
            data.checkType<SerializeObject, TextureUVMapping> {
                TextureUVMapping(
                    corner = Corner.deserialization(it.requireKey("corner")).getOrThrow(),
                    uvMapping = UVMapping.deserialization(it).getOrThrow()
                )
            }
        }

    }

    override fun toString(): String {
        return "TextureUVMapping(corner=$corner,uStart=$uStart, vStart=$vStart, uEnd=$uEnd, vEnd=$vEnd)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as TextureUVMapping

        return corner == other.corner && super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + corner.hashCode()
        return result
    }
}