package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3f

open class VertexImpl(vector3: Vector3f) : Vertex {

	override val x: Float = vector3.x

	override val y: Float = vector3.y

	override val z: Float = vector3.z
	override fun x(x: Number): Vertex = VertexImpl(vector3f().x(x.toFloat()))

	override fun y(y: Number): Vertex = VertexImpl(vector3f().y(y.toFloat()))

	override fun z(z: Number): Vertex = VertexImpl(vector3f().z(z.toFloat()))

	override fun xyz(x: Number, y: Number, z: Number): Vertex =
		VertexImpl(vector3f().xyz(x.toFloat(), y.toFloat(), z.toFloat()))

}