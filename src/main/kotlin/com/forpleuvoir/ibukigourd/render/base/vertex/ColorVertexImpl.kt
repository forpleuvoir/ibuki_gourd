package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.nebula.common.color.Color

class ColorVertexImpl(vector3: Vector3f, override val color: Color) : ColorVertex {

	constructor(x: Number, y: Number, z: Number, color: Color) : this(Vector3f(x, y, z), color)

	override val x: Float = vector3.x

	override val y: Float = vector3.y

	override val z: Float = vector3.z

	override fun x(x: Number): ColorVertex = ColorVertexImpl(vector3f().x(x.toFloat()), color)

	override fun y(y: Number): ColorVertex = ColorVertexImpl(vector3f().y(y.toFloat()), color)

	override fun z(z: Number): ColorVertex = ColorVertexImpl(vector3f().z(z.toFloat()), color)

	override fun xyz(x: Number, y: Number, z: Number): ColorVertex =
		ColorVertexImpl(vector3f().xyz(x.toFloat(), y.toFloat(), z.toFloat()), color)

	override fun color(color: Color): ColorVertex = ColorVertexImpl(vector3f(), color)

}
