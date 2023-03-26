@file:Suppress("UNUSED")

package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f

interface Vertex {

	val x: Float

	val y: Float

	val z: Float

	fun vector3f(): Vector3f = Vector3f(x, y, z)

	fun x(x: Number): Vertex

	fun y(y: Number): Vertex

	fun z(z: Number): Vertex

	fun xyz(x: Number = this.x, y: Number = this.y, z: Number = this.z): Vertex

}

fun vertex(vector3: Vector3f) = VertexImpl(vector3)

fun vertex(vector3: Vector3<Number>) = VertexImpl(Vector3f(vector3))

