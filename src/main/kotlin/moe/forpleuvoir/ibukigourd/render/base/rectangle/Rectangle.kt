package moe.forpleuvoir.ibukigourd.render.base.rectangle

import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.SizeFloat
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.vertex.ColorVertex
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex
import moe.forpleuvoir.nebula.common.color.ARGBColor
import kotlin.math.max
import kotlin.math.min

interface Rectangle<V : Vector3<Float>> : SizeFloat, Cloneable {

	val position: Vector3<Float>

	val vertexes: Array<V>

	override val width: Float

	override val height: Float

	val top: Float get() = position.y

	val bottom: Float get() = position.y + height

	val left: Float get() = position.x

	val right: Float get() = position.x + width

	val x: Float get() = position.x

	val endX: Float get() = x + width

	val y: Float get() = position.y

	val endY: Float get() = y + height

	val z: Float get() = position.z

	val center: Vector3<Float> get() = vertex(x + this.width / 2, y + this.height / 2, z)

	val exist: Boolean get() = this.width > 0 && this.height > 0


	companion object {


		val NULL = rect(0, 0, 0, 0)

		/**
		 * 只判断矩形的位置与大小
		 * @param rectangle Rectangle
		 * @param other Rectangle
		 * @return Boolean
		 */
		fun equals(rectangle: Rectangle<*>, other: Rectangle<*>): Boolean {
			if (rectangle.position != other.position) return false
			if (rectangle.width != other.width) return false
			return rectangle.height == other.height
		}

		infix fun Rectangle<Vector3<Float>>.intersection(target: Rectangle<Vector3<Float>>): Rectangle<Vector3<Float>> {
			if (!this.exist || !target.exist) return NULL
			val startX = max(this.x, target.x)
			val startY = max(this.y, target.y)
			val endX = min(this.endX, target.endY)
			val endY = min(this.endY, target.endY)
			rect(startX, startY, endX, endY).let {
				return if (it.exist) it else NULL
			}
		}

		fun intersection(rects: Iterable<Rectangle<Vector3<Float>>>): Rectangle<Vector3<Float>> {
			var temp = rects.first()
			rects.forEach {
				if (!equals(it, rects.first())) {
					temp = it intersection temp
				}
				if (!temp.exist) return NULL
			}
			return temp
		}
	}

}


fun rect(position: Vector3<Float>, width: Float, height: Float): Rectangle<Vector3<Float>> = RectImpl(position, width, height)

fun rect(position: Vector3<Float>, width: Number, height: Number): Rectangle<Vector3<Float>> = RectImpl(position, width.toFloat(), height.toFloat())

fun rect(x: Number, y: Number, z: Number, width: Number, height: Number): Rectangle<Vector3<Float>> = RectImpl(x, y, z, width, height)

fun rect(startX: Number, startY: Number, endX: Number, endY: Number): Rectangle<Vector3<Float>> =
	RectImpl(startX, startY, 0f, endX.toFloat() - startX.toFloat(), endY.toFloat() - startY.toFloat())

fun rect(x: Number, y: Number, z: Number, size: Size<Number>): Rectangle<Vector3<Float>> = RectImpl(x, y, z, size)

fun rect(position: Vector3<Float>, size: Size<Float>): Rectangle<Vector3<Float>> = RectImpl(position, size)

fun colorRect(colorVertex: ColorVertex, width: Float, height: Float): Rectangle<ColorVertex> =
	ColorRect(colorVertex, width, height, colorVertex.color, colorVertex.color, colorVertex.color, colorVertex.color)

fun colorRect(position: Vector3<Float>, width: Float, height: Float, vararg colors: ARGBColor): Rectangle<ColorVertex> =
	ColorRect(position, width, height, *colors)

fun colorRect(x: Number, y: Number, z: Number, width: Number, height: Number, vararg colors: ARGBColor): Rectangle<ColorVertex> =
	ColorRect(x, y, z, width, height, *colors)

fun colorRect(x: Number, y: Number, z: Number, size: Size<Float>, vararg colors: ARGBColor): Rectangle<ColorVertex> =
	ColorRect(x, y, z, size.width, size.height, *colors)

fun colorRect(rect: Rectangle<Vector3<Float>>, vararg colors: ARGBColor): Rectangle<ColorVertex> =
	ColorRect(rect.x, rect.y, rect.z, rect.width, rect.height, *colors)

fun colorRect(position: Vector3<Float>, size: Size<Float>, vararg colors: ARGBColor): Rectangle<ColorVertex> = ColorRect(position, size, *colors)