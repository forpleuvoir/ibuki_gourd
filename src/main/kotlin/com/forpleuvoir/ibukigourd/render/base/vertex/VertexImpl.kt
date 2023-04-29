package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3f

open class VertexImpl(vector3: Vector3f) : Vertex {

	constructor(x: Number, y: Number, z: Number) : this(Vector3f(x, y, z))

	override val x: Float = vector3.x

	override val y: Float = vector3.y

	override val z: Float = vector3.z
	override fun x(x: Number): Vertex = VertexImpl(vector3f().x(x.toFloat()))

	override fun y(y: Number): Vertex = VertexImpl(vector3f().y(y.toFloat()))

	override fun z(z: Number): Vertex = VertexImpl(vector3f().z(z.toFloat()))

	override fun xyz(x: Number, y: Number, z: Number): Vertex =
		VertexImpl(vector3f().xyz(x.toFloat(), y.toFloat(), z.toFloat()))

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as VertexImpl

		if (x != other.x) return false
		if (y != other.y) return false
		return z == other.z
	}

	override fun hashCode(): Int {
		var result = x.hashCode()
		result = 31 * result + y.hashCode()
		result = 31 * result + z.hashCode()
		return result
	}


}