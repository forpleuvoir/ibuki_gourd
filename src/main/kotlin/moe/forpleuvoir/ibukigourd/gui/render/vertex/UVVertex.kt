package moe.forpleuvoir.ibukigourd.gui.render.vertex

import moe.forpleuvoir.ibukigourd.render.math.Vector3f
import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject
import org.joml.Vector3fc

interface UVVertex : Vector3fc, Serializable {

    companion object : Deserializer<UVVertex> {

        override fun deserialization(serializeElement: SerializeElement): UVVertex {
            return serializeElement
                .checkType<UVVertex>()
                .check<SerializeObject> {
                    val x = it["x"]!!.asFloat
                    val y = it["y"]!!.asFloat
                    val z = it["z"]!!.asFloat
                    val u = it["u"]!!.asFloat
                    val v = it["v"]!!.asFloat
                    UVVertex(x, y, z, u, v)
                }.getOrThrow()
        }

    }

    val u: Float

    val v: Float

    fun copy(x: Number = this.x(), y: Number = this.y(), z: Number = this.z(), u: Number = this.u, v: Number = this.v): UVVertex

}

fun UVVertex(x: Number, y: Number, z: Number, u: Number, v: Number) = UVVertexImpl(x.toFloat(), y.toFloat(), z.toFloat(), u.toFloat(), v.toFloat())

fun UVVertex(vector3: Vector3fc, u: Float, v: Float) = UVVertexImpl(vector3, u, v)

class UVVertexImpl(
    x: Float,
    y: Float,
    z: Float,
    override val u: Float,
    override val v: Float
) : UVVertex, Vector3fc by Vector3f(x, y, z) {

    constructor(vector3fc: Vector3fc, u: Float, v: Float) : this(vector3fc.x(), vector3fc.y(), vector3fc.z(), u, v)

    override fun serialization(): SerializeElement {
        return serializeObject {
            "x" to x()
            "y" to y()
            "z" to z()
            "u" to u
            "v" to v
        }
    }

    override fun copy(x: Number, y: Number, z: Number, u: Number, v: Number): UVVertex {
        return UVVertex(x, y, z, u, v)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UVVertex

        if (u != other.u) return false
        if (v != other.v) return false
        if (x() != other.x()) return false
        if (y() != other.y()) return false
        return z() == other.z()
    }

    override fun hashCode(): Int {
        var result = u.hashCode()
        result = 31 * result + v.hashCode()
        result = 31 * result + x().hashCode()
        result = 31 * result + y().hashCode()
        result = 31 * result + z().hashCode()
        return result
    }

}

