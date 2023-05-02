package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.nebula.common.color.ARGBColor
import com.forpleuvoir.nebula.common.color.Colors

interface UVColorVertex : UVVertex, ColorVertex {
	override fun x(x: Float): UVColorVertex

	override fun y(y: Float): UVColorVertex

	override fun z(z: Float): UVColorVertex

	override fun xyz(x: Float, y: Float, z: Float): UVColorVertex

	override fun plus(x: Float, y: Float, z: Float): UVColorVertex

	override fun unaryPlus(): UVColorVertex

	override fun minus(x: Float, y: Float, z: Float): UVColorVertex

	override fun unaryMinus(): UVColorVertex

	override fun times(x: Float, y: Float, z: Float): UVColorVertex

	override fun div(x: Float, y: Float, z: Float): UVColorVertex

	override fun rem(x: Float, y: Float, z: Float): UVColorVertex

	override fun color(color: ARGBColor): UVColorVertex

	override fun u(u: Float): UVColorVertex

	override fun v(v: Float): UVColorVertex

	override fun uv(u: Float, v: Float): UVColorVertex

}

fun uvColorVertex(
	x: Number,
	y: Number,
	z: Number,
	u: Number,
	v: Number,
	color: ARGBColor = Colors.WHITE
): UVColorVertex = UVColorVertexImpl(x, y, z, u, v, color)


fun uvColorVertex(
	vector3: Vector3f,
	u: Float,
	v: Float,
	color: ARGBColor = Colors.WHITE
): UVColorVertex = UVColorVertexImpl(vector3, u, v, color)

fun uvColorVertex(
	vector3: Vector3<Number>,
	u: Float,
	v: Float,
	color: ARGBColor = Colors.WHITE
): UVColorVertex = UVColorVertexImpl(Vector3f(vector3), u, v, color)
