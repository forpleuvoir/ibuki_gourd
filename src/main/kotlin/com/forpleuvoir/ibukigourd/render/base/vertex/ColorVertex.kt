package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.nebula.common.color.ARGBColor

interface ColorVertex : Vector3<Float> {

	override fun x(x: Float): ColorVertex

	override fun y(y: Float): ColorVertex

	override fun z(z: Float): ColorVertex

	override fun xyz(x: Float, y: Float, z: Float): ColorVertex

	override fun plus(x: Float, y: Float, z: Float): ColorVertex

	override fun unaryPlus(): ColorVertex

	override fun minus(x: Float, y: Float, z: Float): ColorVertex

	override fun unaryMinus(): ColorVertex

	override fun times(x: Float, y: Float, z: Float): ColorVertex

	override fun div(x: Float, y: Float, z: Float): ColorVertex

	override fun rem(x: Float, y: Float, z: Float): ColorVertex

	val color: ARGBColor

	fun color(color: ARGBColor): ColorVertex

}

fun colorVertex(
	x: Number,
	y: Number,
	z: Number,
	color: ARGBColor
) = ColorVertexImpl(x, y, z, color)

fun colorVertex(
	vector3: Vector3<Float>,
	color: ARGBColor
) = ColorVertexImpl(Vector3f(vector3), color)