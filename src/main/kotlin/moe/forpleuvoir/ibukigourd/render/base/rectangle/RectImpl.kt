package moe.forpleuvoir.ibukigourd.render.base.rectangle

import moe.forpleuvoir.ibukigourd.render.base.Size
import moe.forpleuvoir.ibukigourd.render.base.math.ImmutableVector3f
import moe.forpleuvoir.ibukigourd.render.base.math.Vector3
import moe.forpleuvoir.ibukigourd.render.base.vertex.vertex

class RectImpl(
	override val position: Vector3<Float>,
	override val width: Float,
	override val height: Float
) : Rectangle<Vector3<Float>> {

	constructor(x: Number, y: Number, z: Number, width: Number, height: Number) : this(vertex(x, y, z), width.toFloat(), height.toFloat())

	constructor(x: Number, y: Number, z: Number, size: Size<Number>) : this(vertex(x, y, z), size.width.toFloat(), size.height.toFloat())

	constructor(position: Vector3<Float>, size: Size<Float>) : this(ImmutableVector3f(position.x, position.y, position.z), size.width, size.height)

	override val vertexes: Array<Vector3<Float>>
		get() = arrayOf(
			position,
			position.y(y + height),
			position.xyz(x + width, y + height),
			position.x(x + width)
		)

}