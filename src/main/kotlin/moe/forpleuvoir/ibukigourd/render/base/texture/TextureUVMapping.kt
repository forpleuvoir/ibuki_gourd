package moe.forpleuvoir.ibukigourd.render.base.texture

import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType

open class TextureUVMapping(
    val corner: Corner,
    u1: Int, v1: Int, u2: Int, v2: Int
) : UVMapping(u1, v1, u2, v2) {
    constructor(corner: Corner, uvMapping: UVMapping) : this(
        corner,
        uvMapping.uStart, uvMapping.vStart, uvMapping.uEnd, uvMapping.vEnd
    )

    companion object {

        fun deserialization(serializeElement: SerializeElement?, default: TextureUVMapping): TextureUVMapping {
            return serializeElement?.run {
                checkType {
                    check<SerializeObject> {
                        TextureUVMapping(
                            corner = Corner.deserialization(it["corner"], default.corner),
                            uvMapping = UVMapping.deserialization(it, default)
                        )
                    }
                }.getOrDefault(default)
            } ?: default
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

        return corner == other.corner
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + corner.hashCode()
        return result
    }

}