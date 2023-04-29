package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.nebula.common.color.Color
import com.forpleuvoir.nebula.common.color.Colors

class UVColorVertexImpl(
	vector3: Vector3f,
	override val u: Float,
	override val v: Float,
	override val color: Color = Colors.WHITE
) : UVColorVertex {

	constructor(x: Number, y: Number, z: Number, u: Number, v: Number, color: Color = Colors.WHITE) : this(Vector3f(x, y, z), u.toFloat(), v.toFloat(), color)

	override val x: Float = vector3.x

	override val y: Float = vector3.y

	override val z: Float = vector3.z
	override fun x(x: Number): UVColorVertex {
		return UVColorVertexImpl(vector3f().x(x.toFloat()), u, v, color)
	}

	override fun y(y: Number): UVColorVertex {
		return UVColorVertexImpl(vector3f().y(y.toFloat()), u, v, color)
	}

	override fun z(z: Number): UVColorVertex {
		return UVColorVertexImpl(vector3f().z(z.toFloat()), u, v, color)
	}

	override fun xyz(x: Number, y: Number, z: Number): UVColorVertex {
		return UVColorVertexImpl(vector3f().xyz(x.toFloat(), y.toFloat(), z.toFloat()), u, v, color)
	}

	override fun u(u: Float): UVColorVertex {
		return UVColorVertexImpl(vector3f(), u, v, color)
	}

	override fun v(v: Float): UVColorVertex {
		return UVColorVertexImpl(vector3f(), u, v, color)
	}

	override fun uv(u: Float, v: Float): UVColorVertex {
		return UVColorVertexImpl(vector3f(), u, v, color)
	}

	override fun color(color: Color): UVColorVertex {
		return UVColorVertexImpl(vector3f(), u, v, color)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as UVColorVertexImpl

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