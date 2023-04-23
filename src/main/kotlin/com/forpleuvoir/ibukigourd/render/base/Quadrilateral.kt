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
	position.y(position.y + height.toFloat()),
	position.xyz(position.x + width.toFloat(), position.y + height.toFloat()),
	position.x(position.x + width.toFloat())
) {

	val width: Float = width.toFloat()

	val height: Float = height.toFloat()

	val top: Float get() = position.y

	val bottom: Float get() = position.y + height

	val left: Float get() = position.x

	val right: Float get() = position.x + width

	val x: Float get() = position.x

	val y: Float get() = position.y

	val z: Float get() = position.z

	val center: Vector3f get() = Vector3f(x + this.width / 2, y + this.height / 2, z)

}

val Iterable<Rectangle>.maxHeight: Float
	get() = this.minByOrNull { it.height }!!.height

val Iterable<Rectangle>.maxWidth: Float
	get() = this.minByOrNull { it.width }!!.width
