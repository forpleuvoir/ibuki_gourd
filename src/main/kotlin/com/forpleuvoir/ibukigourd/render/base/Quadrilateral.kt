package com.forpleuvoir.ibukigourd.render.base

import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.ibukigourd.render.base.vertex.Vertex
import com.forpleuvoir.ibukigourd.render.base.vertex.vertex

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

	constructor(x: Number, y: Number, z: Number, width: Number, height: Number) : this(vertex(x, y, z), width, height)

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

	companion object {

		/**
		 * 只判断矩形的位置与大小
		 * @param rectangle Rectangle
		 * @param other Rectangle
		 * @return Boolean
		 */
		fun equals(rectangle: Rectangle, other: Rectangle): Boolean {
			if (rectangle.position != other.position) return false
			if (rectangle.width != other.width) return false
			return rectangle.height == other.height
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Rectangle

		if (position != other.position) return false
		if (width != other.width) return false
		return height == other.height
	}

	override fun hashCode(): Int {
		var result = position.hashCode()
		result = 31 * result + width.hashCode()
		result = 31 * result + height.hashCode()
		return result
	}


}

val Iterable<Rectangle>.maxHeight: Float
	get() = this.maxOf { it.height }

val Iterable<Rectangle>.maxWidth: Float
	get() = this.maxOf { it.width }
