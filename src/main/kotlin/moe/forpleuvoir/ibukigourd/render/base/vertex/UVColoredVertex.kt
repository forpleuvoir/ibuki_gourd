package moe.forpleuvoir.ibukigourd.render.base.vertex

import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.deserialization
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject
import org.joml.Vector3f
import org.joml.Vector3fc

interface UVColoredVertex : UVVertex, ColoredVertex {

    companion object : Deserializer<UVColoredVertex> {

        override fun deserialization(serializeElement: SerializeElement): UVColoredVertex {
            return serializeElement
                .checkType<UVColoredVertex>()
                .check<SerializeObject> {
                    UVColoredVertex(
                        it["x"]!!.asFloat,
                        it["y"]!!.asFloat,
                        it["z"]!!.asFloat,
                        it["u"]!!.asFloat,
                        it["v"]!!.asFloat,
                        Color.deserialization(it["color"]!!)
                    )
                }.getOrThrow()
        }

    }

    fun copy(x: Number = this.x(), y: Number = this.y(), z: Number = this.z(), u: Number = this.u, v: Number = this.v, color: ARGBColor = this.color): UVColoredVertex

}

fun UVColoredVertex(
    x: Number,
    y: Number,
    z: Number,
    u: Number,
    v: Number,
    color: ARGBColor
): UVColoredVertex = UVColorVertexImpl(x.toFloat(), y.toFloat(), z.toFloat(), u.toFloat(), v.toFloat(), color)

fun UVColoredVertex(
    vector3: Vector3f,
    u: Float,
    v: Float,
    color: ARGBColor
): UVColoredVertex = UVColorVertexImpl(vector3, u, v, color)

class UVColorVertexImpl(
    x: Float,
    y: Float,
    z: Float,
    override val u: Float,
    override val v: Float,
    override val color: ARGBColor
) : UVColoredVertex, Vector3fc by Vector3f(x, y, z) {

    constructor(vector3fc: Vector3fc, u: Float, v: Float, color: ARGBColor) : this(vector3fc.x(), vector3fc.y(), vector3fc.z(), u, v, color)

    override fun serialization(): SerializeElement {
        return serializeObject {
            "x" to x()
            "y" to y()
            "z" to z()
            "u" to u
            "v" to v
            "color" to color
        }
    }


    override fun copy(x: Number, y: Number, z: Number, u: Number, v: Number, color: ARGBColor): UVColoredVertex {
        return UVColoredVertex(x, y, z, u, v, color)
    }

    override fun copy(x: Number, y: Number, z: Number, u: Number, v: Number): UVVertex {
        return UVVertex(x, y, z, u, v)
    }

    override fun copy(x: Number, y: Number, z: Number, color: ARGBColor): ColoredVertex {
        return ColoredVertex(x, y, z, color)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UVColoredVertex

        if (u != other.u) return false
        if (v != other.v) return false
        if (color != other.color) return false
        if (x() != other.x()) return false
        if (y() != other.y()) return false
        return z() == other.z()
    }

    override fun hashCode(): Int {
        var result = u.hashCode()
        result = 31 * result + v.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + x().hashCode()
        result = 31 * result + y().hashCode()
        result = 31 * result + z().hashCode()
        return result
    }

}
