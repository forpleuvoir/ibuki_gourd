package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f

interface UVVertex : Vertex {

	val u: Float

	val v: Float

	override fun x(x: Number): UVVertex

	override fun y(y: Number): UVVertex

	override fun z(z: Number): UVVertex

	override fun xyz(x: Number, y: Number, z: Number): UVVertex

	fun u(u: Float): UVVertex

	fun v(v: Float): UVVertex

	fun uv(u: Float = this.u, v: Float): UVVertex

}

fun uvVertex(
	vector3: Vector3f,
	u: Float,
	v: Float
) = UVVertexImpl(vector3, u, v)

fun uvVertex(
	vector3: Vector3<Number>,
	u: Float,
	v: Float
) = UVVertexImpl(Vector3f(vector3), u, v)