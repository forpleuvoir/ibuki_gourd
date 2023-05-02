package com.forpleuvoir.ibukigourd.render.base.rectangle

import com.forpleuvoir.ibukigourd.render.base.Size
import com.forpleuvoir.ibukigourd.render.base.math.ImmutableVector3f
import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.vertex.ColorVertex
import com.forpleuvoir.ibukigourd.render.base.vertex.colorVertex
import com.forpleuvoir.ibukigourd.render.base.vertex.vertex
import com.forpleuvoir.nebula.common.color.ARGBColor

class ColorRect(
	override val position: Vector3<Float>,
	override val width: Float,
	override val height: Float,
	private vararg val colors: ARGBColor
) : Rectangle<ColorVertex> {

	constructor(x: Number, y: Number, z: Number, width: Number, height: Number, vararg colors: ARGBColor) : this(
		vertex(x, y, z),
		width.toFloat(),
		height.toFloat(),
		*colors
	)

	constructor(x: Number, y: Number, z: Number, size: Size<Number>, vararg colors: ARGBColor) : this(
		vertex(x, y, z),
		size.width.toFloat(),
		size.height.toFloat(),
		*colors
	)

	constructor(position: Vector3<Float>, size: Size<Float>, vararg colors: ARGBColor) : this(
		ImmutableVector3f(position.x, position.y, position.z),
		size.width,
		size.height,
		*colors
	)

	override val vertexes: Array<ColorVertex>
		get() = arrayOf(
			colorVertex(position, colors[0]),
			colorVertex(position.y(y + height), colors[1]),
			colorVertex(position.xyz(x + width, y + height), colors[2]),
			colorVertex(position.x(x + width), colors[3])
		)

}