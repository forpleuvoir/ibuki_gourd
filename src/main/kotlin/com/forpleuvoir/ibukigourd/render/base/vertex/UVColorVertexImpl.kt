package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.nebula.common.color.ARGBColor
import com.forpleuvoir.nebula.common.color.Colors

class UVColorVertexImpl(
	vector3: Vector3<Float>,
	override val u: Float,
	override val v: Float,
	override val color: ARGBColor = Colors.WHITE
) : UVColorVertex {

	constructor(x: Number, y: Number, z: Number, u: Number, v: Number, color: ARGBColor = Colors.WHITE) : this(
		Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
		u.toFloat(),
		v.toFloat(),
		color
	)

	override val x: Float = vector3.x

	override val y: Float = vector3.y

	override val z: Float = vector3.z

	override fun x(x: Float): UVColorVertexImpl {
		return UVColorVertexImpl(x, y, z, u, v, color)
	}

	override fun y(y: Float): UVColorVertexImpl {
		return UVColorVertexImpl(x, y, z, u, v, color)
	}

	override fun z(z: Float): UVColorVertexImpl {
		return UVColorVertexImpl(x, y, z, u, v, color)
	}

	override fun xyz(x: Float, y: Float, z: Float): UVColorVertexImpl {
		return UVColorVertexImpl(x, y, z, u, v, color)
	}

	override fun plus(vector3: Vector3<Float>): UVColorVertexImpl {
		return this.plus(vector3.x, vector3.y, vector3.z)
	}

	override fun minus(vector3: Vector3<Float>): UVColorVertexImpl {
		return this.minus(vector3.x, vector3.y, vector3.z)
	}

	override fun times(vector3: Vector3<Float>): UVColorVertexImpl {
		return this.times(vector3.x, vector3.y, vector3.z)
	}

	override fun div(vector3: Vector3<Float>): UVColorVertexImpl {
		return this.div(vector3.x, vector3.y, vector3.z)
	}

	override fun rem(vector3: Vector3<Float>): UVColorVertexImpl {
		return this.rem(vector3.x, vector3.y, vector3.z)
	}

	override fun plus(x: Float, y: Float, z: Float): UVColorVertexImpl {
		return UVColorVertexImpl(this.x + x, this.y + y, this.z + z, u, v, color)
	}

	override fun unaryPlus(): UVColorVertexImpl {
		return UVColorVertexImpl(+x, +y, +z, u, v, color)
	}

	override fun minus(x: Float, y: Float, z: Float): UVColorVertexImpl {
		return UVColorVertexImpl(this.x - x, this.y - y, this.z - z, u, v, color)
	}

	override fun unaryMinus(): UVColorVertexImpl {
		return UVColorVertexImpl(-x, -y, -z, u, v, color)
	}

	override fun times(x: Float, y: Float, z: Float): UVColorVertexImpl {
		return UVColorVertexImpl(this.x * x, this.y * y, this.z * z, u, v, color)
	}

	override fun div(x: Float, y: Float, z: Float): UVColorVertexImpl {
		return UVColorVertexImpl(this.x / x, this.y / y, this.z / z, u, v, color)
	}

	override fun rem(x: Float, y: Float, z: Float): UVColorVertexImpl {
		return UVColorVertexImpl(this.x % x, this.y % y, this.z % z, u, v, color)
	}

	override fun u(u: Float): UVColorVertexImpl {
		return UVColorVertexImpl(this, u, v, color)
	}

	override fun v(v: Float): UVColorVertexImpl {
		return UVColorVertexImpl(this, u, v, color)
	}

	override fun uv(u: Float, v: Float): UVColorVertexImpl {
		return UVColorVertexImpl(this, u, v, color)
	}

	override fun color(color: ARGBColor): UVColorVertexImpl {
		return UVColorVertexImpl(this, u, v, color)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as UVColorVertex

		if (u != other.u) return false
		if (v != other.v) return false
		if (color != other.color) return false
		if (x != other.x) return false
		if (y != other.y) return false
		return z == other.z
	}

	override fun hashCode(): Int {
		var result = u.hashCode()
		result = 31 * result + v.hashCode()
		result = 31 * result + color.hashCode()
		result = 31 * result + x.hashCode()
		result = 31 * result + y.hashCode()
		result = 31 * result + z.hashCode()
		return result
	}


}