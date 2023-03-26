package com.forpleuvoir.ibukigourd.render.base

import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.base.vertex.Vertex

open class Quadrilateral(
	val vertex1: Vertex,
	val vertex2: Vertex,
	val vertex3: Vertex,
	val vertex4: Vertex,
)

open class Rectangle(
	val position: Vertex,
	width: Number,
	height: Number
) : Quadrilateral(
	position,
	position.x(position.x + width.toFloat()),
	position.y(position.y + height.toFloat()),
	position.xyz(position.x + width.toFloat(), position.y + height.toFloat())
) {
	val width: Float = height.toFloat()

	val height: Float = height.toFloat()

	val top: Float get() = position.y

	val bottom: Float get() = position.y + height

	val left: Float get() = position.x

	val right: Float get() = position.x + width

	val x = position.x

	val y = position.y

	val z = position.z

	val center: Vector3f get() = Vector3f(this.width / 2, this.height / 2, z)


}