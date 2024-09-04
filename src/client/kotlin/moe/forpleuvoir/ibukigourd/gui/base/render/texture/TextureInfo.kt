package moe.forpleuvoir.ibukigourd.gui.base.render.texture

import moe.forpleuvoir.ibukigourd.gui.base.render.SizeInt
import moe.forpleuvoir.ibukigourd.util.identifier
import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject
import net.minecraft.util.Identifier

data class TextureInfo(
    override val width: Int = 256,
    override val height: Int = 256,
    val texture: Identifier
) : Serializable, SizeInt {

    companion object : Deserializer<TextureInfo> {
        override fun deserialization(serializeElement: SerializeElement): TextureInfo {
            return serializeElement.checkType {
                check<SerializeObject> {
                    TextureInfo(
                        it["width"]!!.asInt,
                        it["height"]!!.asInt,
                        identifier(it["texture"]!!.asString)
                    )
                }
            }.getOrThrow()
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