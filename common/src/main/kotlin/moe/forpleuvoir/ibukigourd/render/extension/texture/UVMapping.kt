package moe.forpleuvoir.ibukigourd.render.extension.texture

import moe.forpleuvoir.nebula.common.util.expectedType
import moe.forpleuvoir.nebula.common.util.letNotNull
import moe.forpleuvoir.nebula.common.util.requireTypeOrNull
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.base.builder.build
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.extensions.requireInt
import org.joml.Vector4f

open class UVMapping(
    val uStart: Int,
    val vStart: Int,
    val uEnd: Int,
    val vEnd: Int,
) {

    companion object : Codec<UVMapping> {

        fun uv(u: Int, v: Int, uSize: Int, vSize: Int) =
            UVMapping(u, v, u + uSize, v + vSize)

        override fun deserialization(data: SerializeElement): Result<UVMapping> = DeserializationException.runCatching {
            data.requireTypeOrNull<SerializeObject>().letNotNull {
                it.run {
                    val uStart: Int
                    val uEnd: Int
                    if (containsKey("u") && containsKey("u_size")) {
                        uStart = requireInt("u")
                        uEnd = requireInt("u_size") + uStart
                    } else if (containsKey("u") && containsKey("width")) {
                        uStart = requireInt("u")
                        uEnd = requireInt("width") + uStart
                    } else {
                        uStart = requireInt("u_start")
                        uEnd = requireInt("u_end")
                    }
                    val vStart: Int
                    val vEnd: Int
                    if (containsKey("v") && containsKey("v_size")) {
                        vStart = requireInt("v")
                        vEnd = requireInt("v_size") + vStart
                    } else if (containsKey("v") && containsKey("height")) {
                        vStart = requireInt("u")
                        vEnd = requireInt("height") + vStart
                    } else {
                        vStart = requireInt("v_start")
                        vEnd = requireInt("v_end")
                    }
                    UVMapping(uStart, vStart, uEnd, vEnd)
                }
            } ?: throw expectedType(data::class, SerializeObject::class)
        }

        override fun serialization(target: UVMapping): SerializeElement = SerializeObject.build {
            "u_size" to target.uStart
            "v_start" to target.vStart
            "u_end" to target.uEnd
            "v_end" to target.vEnd
        }
    }

    val uSize = uEnd - uStart

    val vSize = vEnd - vStart

    val width: Int by ::uSize

    val height: Int by ::vSize

    fun mapping(textureWidth: Int, textureHeight: Int): Vector4f {
        return Vector4f(
            uStart.toFloat() / textureWidth,
            vStart.toFloat() / textureHeight,
            uSize.toFloat() / textureWidth,
            vSize.toFloat() / textureHeight
        )
    }

    override fun toString(): String {
        return "UVMapping(uStart=$uStart, vStart=$vStart, uEnd=$uEnd, vEnd=$vEnd, uSize=$uSize, vSize=$vSize)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UVMapping

        if (uStart != other.uStart) return false
        if (vStart != other.vStart) return false
        if (uEnd != other.uEnd) return false
        return vEnd == other.vEnd
    }

    override fun hashCode(): Int {
        var result = uStart
        result = 31 * result + vStart
        result = 31 * result + uEnd
        result = 31 * result + vEnd
        return result
    }

}