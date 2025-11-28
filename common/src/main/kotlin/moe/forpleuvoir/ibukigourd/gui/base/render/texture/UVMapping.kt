package moe.forpleuvoir.ibukigourd.gui.base.render.texture

import moe.forpleuvoir.ibukigourd.gui.base.render.SizeInt
import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject
import org.joml.Vector4f

open class UVMapping(
    val uStart: Int,
    val vStart: Int,
    val uEnd: Int,
    val vEnd: Int,
) : SizeInt, Serializable {

    companion object : Deserializer<UVMapping> {

        fun uv(u: Int, v: Int, uSize: Int, vSize: Int) =
            UVMapping(u, v, u + uSize, v + vSize)

        override fun deserialization(serializeElement: SerializeElement): UVMapping {
            return serializeElement.checkType {
                check<SerializeObject> {
                    it.run {
                        val uStart: Int
                        val uEnd: Int
                        if (containsKey("u") && containsKey("u_size")) {
                            uStart = get("u")!!.asInt
                            uEnd = get("u_size")!!.asInt + uStart
                        } else if (containsKey("u") && containsKey("width")) {
                            uStart = get("u")!!.asInt
                            uEnd = get("width")!!.asInt + uStart
                        } else {
                            uStart = this["u_start"]!!.asInt
                            uEnd = this["u_end"]!!.asInt
                        }
                        val vStart: Int
                        val vEnd: Int
                        if (containsKey("v") && containsKey("v_size")) {
                            vStart = get("v")!!.asInt
                            vEnd = get("v_size")!!.asInt + vStart
                        } else if (containsKey("v") && containsKey("height")) {
                            vStart = get("u")!!.asInt
                            vEnd = get("height")!!.asInt + vStart
                        } else {
                            vStart = this["v_start"]!!.asInt
                            vEnd = this["v_end"]!!.asInt
                        }
                        UVMapping(uStart, vStart, uEnd, vEnd)
                    }
                }
            }.getOrThrow()
        }
    }

    val uSize = uEnd - uStart

    val vSize = vEnd - vStart

    fun mapping(textureWidth: Int, textureHeight: Int): Vector4f {
        return Vector4f(
            uStart.toFloat() / textureWidth,
            vStart.toFloat() / textureHeight,
            uSize.toFloat() / textureWidth,
            vSize.toFloat() / textureHeight
        )
    }

    override fun serialization(): SerializeElement {
        return serializeObject {
            "uStart" to uStart
            "vStart" to vStart
            "uEnd" to uEnd
            "vEnd" to vEnd
        }
    }

    override fun toString(): String {
        return "UVMapping(uStart=$uStart, vStart=$vStart, uEnd=$uEnd, vEnd=$vEnd, uSize=$uSize, vSize=$vSize)"
    }

    override val width: Int by ::uSize

    override val height: Int by ::vSize

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