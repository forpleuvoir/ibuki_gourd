package moe.forpleuvoir.ibukigourd.render.base.texture

import moe.forpleuvoir.ibukigourd.util.resources
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.getOr
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject
import net.minecraft.util.Identifier

data class TextureInfo(
    val width: Int = 256,
    val height: Int = 256,
    val texture: Identifier
) : Serializable {
    companion object {
        fun deserialization(serializeElement: SerializeElement?, default: TextureInfo): TextureInfo {
            return serializeElement?.run {
                checkType {
                    check<SerializeObject> {
                        TextureInfo(
                            it.getOr("width", default.width).toInt(),
                            it.getOr("height", default.height).toInt(),
                            runCatching { resources(it["texture"]!!.asString) }.getOrDefault(default.texture)
                        )
                    }
                }.getOrDefault(default)
            } ?: default
        }
    }

    override fun serialization(): SerializeElement {
        return serializeObject {
            "width" to width
            "height" to height
            "texture" to texture.toString()
        }
    }

    override fun toString(): String {
        return "TextureInfo(width=$width, height=$height, texture=$texture)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextureInfo

        if (width != other.width) return false
        if (height != other.height) return false
        return texture == other.texture
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + texture.hashCode()
        return result
    }

}