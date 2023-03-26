package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3f

class UVVertexImpl(vector3: Vector3f, override val u: Float, override val v: Float) : UVVertex {

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

}