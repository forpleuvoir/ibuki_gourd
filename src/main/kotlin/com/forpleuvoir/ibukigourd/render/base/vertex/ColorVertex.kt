package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.nebula.common.color.Color

interface ColorVertex : Vertex {
	override fun x(x: Number): ColorVertex

	override fun y(y: Number): ColorVertex

	override fun z(z: Number): ColorVertex

	override fun xyz(x: Number, y: Number, z: Number): ColorVertex

	val color: Color

	fun color(color: Color): ColorVertex

}

fun colorVertex(
	vector3: Vector3f,
	color: Color
) = ColorVertexImpl(vector3, color)

fun colorVertex(
	vector3: Vector3<Number>,
	color: Color
) = ColorVertexImpl(Vector3f(vector3), color)