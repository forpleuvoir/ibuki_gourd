package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3f

class UVVertexImpl(vector3: Vector3f, override val u: Float, override val v: Float) : UVVertex {

	constructor(x: Number, y: Number, z: Number, u: Number, v: Number) : this(Vector3f(x, y, z), u.toFloat(), v.toFloat())

	override val x: Float = vector3.x

	override val y: Float = vector3.y

	override val z: Float = vector3.z

	override fun x(x: Number): UVVertex = UVVertexImpl(vector3f().x(x.toFloat()), u, v)

	override fun y(y: Number): UVVertex = UVVertexImpl(vector3f().y(y.toFloat()), u, v)

	override fun z(z: Number): UVVertex = UVVertexImpl(vector3f().z(z.toFloat()), u, v)

	override fun xyz(x: Number, y: Number, z: Number): UVVertex =
		UVVertexImpl(vector3f().xyz(x.toFloat(), y.toFloat(), z.toFloat()), u, v)

	override fun u(u: Float): UVVertex {
		return UVVertexImpl(vector3f(), u, v)
	}

	override fun v(v: Float): UVVertex {
		return UVVertexImpl(vector3f(), u, v)
	}

	override fun uv(u: Float, v: Float): UVVertex {
		return UVVertexImpl(vector3f(), u, v)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as UVVertexImpl

		if (u != other.u) return false
		if (v != other.v) return false
		if (x != other.x) return false
		if (y != other.y) return false
		return z == other.z
	}

	override fun hashCode(): Int {
		var result = u.hashCode()
		result = 31 * result + v.hashCode()
		result = 31 * result + x.hashCode()
		result = 31 * result + y.hashCode()
		result = 31 * result + z.hashCode()
		return result
	}


}