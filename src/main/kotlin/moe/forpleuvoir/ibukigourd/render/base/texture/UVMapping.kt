package moe.forpleuvoir.ibukigourd.render.base.texture

import moe.forpleuvoir.ibukigourd.render.base.DimensionInt
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.getOr
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject

open class UVMapping(
    val uStart: Int,
    val vStart: Int,
    val uEnd: Int,
    val vEnd: Int,
) : DimensionInt, Serializable {

    companion object {

        fun uv(u: Int, v: Int, uSize: Int, vSize: Int) =
            UVMapping(u, v, u + uSize, v + vSize)

        fun deserialization(serializeElement: SerializeElement?, default: UVMapping): UVMapping {
            return serializeElement?.run {
                checkType {
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
                                uStart = getOr("uStart", default.uStart).toInt()
                                uEnd = getOr("uEnd", default.uEnd).toInt()
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
                                vStart = getOr("vStart", default.vStart).toInt()
                                vEnd = getOr("vEnd", default.vEnd).toInt()
                            }
                            UVMapping(uStart, vStart, uEnd, vEnd)
                        }
                    }
                }.getOrDefault(default)
            } ?: default
        }
    }

    val uSize = uEnd - uStart

    val vSize = vEnd - vStart

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