package moe.forpleuvoir.ibukigourd.render.base.vertex

import moe.forpleuvoir.ibukigourd.render.base.math.Vector3f
import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.deserialization
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject
import org.joml.Vector3fc

interface ColoredVertex : Vector3fc, Serializable {

    companion object : Deserializer<ColoredVertex> {

        override fun deserialization(serializeElement: SerializeElement): ColoredVertex {
            return serializeElement
                .checkType<ColoredVertex>()
                .check<SerializeObject> {
                    val x = it["x"]!!.asFloat
                    val y = it["y"]!!.asFloat
                    val z = it["z"]!!.asFloat
                    val color = Color.deserialization(it["color"]!!)
                    ColoredVertex(x, y, z, color)
                }.getOrThrow()
        }

    }

    val color: ARGBColor

    fun copy(x: Number = this.x(), y: Number = this.y(), z: Number = this.z(), color: ARGBColor = this.color): ColoredVertex

}

fun ColoredVertex(x: Number, y: Number, z: Number, color: ARGBColor): ColoredVertex = ColoredVertexImpl(x.toFloat(), y.toFloat(), z.toFloat(), color)

fun ColoredVertex(vector3fc: Vector3fc, color: ARGBColor): ColoredVertex = ColoredVertexImpl(vector3fc, color)

class ColoredVertexImpl(x: Float, y: Float, z: Float, override val color: ARGBColor) : ColoredVertex, Vector3fc by Vector3f(x, y, z) {

    constructor(vector3fc: Vector3fc, color: ARGBColor) : this(vector3fc.x(), vector3fc.y(), vector3fc.z(), color)

    override fun copy(x: Number, y: Number, z: Number, color: ARGBColor): ColoredVertex = ColoredVertex(x, y, z, color)

    override fun serialization(): SerializeElement {
        return serializeObject {
            "x" to x()
            "y" to y()
            "z" to z()
            "color" to color
        }
    }

    override fun equals(other: Any?): Boolean {
        this[0]
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ColoredVertex

        if (color != other.color) return false
        if (x() != other.x()) return false
        if (y() != other.y()) return false
        return z() == other.z()
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + x().hashCode()
        result = 31 * result + y().hashCode()
        result = 31 * result + z().hashCode()
        return result
    }

}
