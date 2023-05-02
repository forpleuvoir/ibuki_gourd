package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f

interface UVVertex : Vector3<Float> {

	override fun x(x: Float): UVVertex

	override fun y(y: Float): UVVertex

	override fun z(z: Float): UVVertex

	override fun xyz(x: Float, y: Float, z: Float): UVVertex

	override fun plus(x: Float, y: Float, z: Float): UVVertex

	override fun unaryPlus(): UVVertex

	override fun minus(x: Float, y: Float, z: Float): UVVertex

	override fun unaryMinus(): UVVertex

	override fun times(x: Float, y: Float, z: Float): UVVertex

	override fun div(x: Float, y: Float, z: Float): UVVertex

	override fun rem(x: Float, y: Float, z: Float): UVVertex

	val u: Float

	val v: Float

	fun u(u: Float): UVVertex

	fun v(v: Float): UVVertex

	fun uv(u: Float = this.u, v: Float = this.v): UVVertex

}

fun uvVertex(
	x: Number,
	y: Number,
	z: Number,
	u: Number,
	v: Number
) = UVVertexImpl(x, y, z, u, v)

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