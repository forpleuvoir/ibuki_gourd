package moe.forpleuvoir.ibukigourd.render.base.vertex

import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.nebula.common.color.ARGBColor

class ColorVertexImpl(vector3: Vector3<Float>, override val color: ARGBColor) : ColorVertex {

	constructor(x: Number, y: Number, z: Number, color: ARGBColor) : this(vertex(x, y, z), color)

	override val x: Float = vector3.x

	override val y: Float = vector3.y

	override val z: Float = vector3.z

	override fun x(x: Float): ColorVertexImpl {
		return ColorVertexImpl(x, this.y, this.z, this.color)
	}

	override fun y(y: Float): ColorVertexImpl {
		return ColorVertexImpl(this.x, y, this.z, this.color)
	}

	override fun z(z: Float): ColorVertexImpl {
		return ColorVertexImpl(this.x, this.y, z, this.color)
	}

	override fun xyz(x: Float, y: Float, z: Float): ColorVertexImpl {
		return ColorVertexImpl(x, y, z, this.color)
	}

	override fun plus(vector3: Vector3<Float>): ColorVertexImpl {
		return this.plus(vector3.x, vector3.y, vector3.z)
	}

	override fun minus(vector3: Vector3<Float>): ColorVertexImpl {
		return this.minus(vector3.x, vector3.y, vector3.z)
	}

	override fun times(vector3: Vector3<Float>): ColorVertexImpl {
		return this.times(vector3.x, vector3.y, vector3.z)
	}

	override fun div(vector3: Vector3<Float>): ColorVertexImpl {
		return this.div(vector3.x, vector3.y, vector3.z)
	}

	override fun rem(vector3: Vector3<Float>): ColorVertexImpl {
		return this.rem(vector3.x, vector3.y, vector3.z)
	}

	override fun plus(x: Float, y: Float, z: Float): ColorVertexImpl {
		return ColorVertexImpl(this.x + x, this.y + y, this.z + z, this.color)
	}

	override fun unaryPlus(): ColorVertexImpl {
		return ColorVertexImpl(+x, +y, +z, this.color)
	}

	override fun minus(x: Float, y: Float, z: Float): ColorVertexImpl {
		return ColorVertexImpl(this.x - x, this.y - y, this.z - z, this.color)
	}

	override fun unaryMinus(): ColorVertexImpl {
		return ColorVertexImpl(-x, -y, -z, this.color)
	}

	override fun times(x: Float, y: Float, z: Float): ColorVertexImpl {
		return ColorVertexImpl(this.x * x, this.y * y, this.z * z, this.color)
	}

	override fun div(x: Float, y: Float, z: Float): ColorVertexImpl {
		return ColorVertexImpl(this.x / x, this.y / y, this.z / z, this.color)
	}

	override fun rem(x: Float, y: Float, z: Float): ColorVertexImpl {
		return ColorVertexImpl(this.x % x, this.y % y, this.z % z, this.color)
	}

	override fun color(color: ARGBColor): ColorVertexImpl = ColorVertexImpl(vertex(x, y, z), color)

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		if (!super.equals(other)) return false

		other as ColorVertex

		if (color != other.color) return false
		if (x != other.x) return false
		if (y != other.y) return false
		return z == other.z
	}

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + color.hashCode()
		result = 31 * result + x.hashCode()
		result = 31 * result + y.hashCode()
		result = 31 * result + z.hashCode()
		return result
	}

}
