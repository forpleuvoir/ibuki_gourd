package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3
import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.nebula.common.color.Color
import com.forpleuvoir.nebula.common.color.Colors

interface UVColorVertex : UVVertex, ColorVertex {

	override fun x(x: Number): UVColorVertex

	override fun y(y: Number): UVColorVertex

	override fun z(z: Number): UVColorVertex

	override fun xyz(x: Number, y: Number, z: Number): UVColorVertex

	override fun u(u: Float): UVColorVertex

	override fun v(v: Float): UVColorVertex

	override fun uv(u: Float, v: Float): UVColorVertex

	override fun color(color: Color): UVColorVertex

}

fun uvColorVertex(
	vector3: Vector3f,
	u: Float,
	v: Float,
	color: Color = Colors.WHITE
): UVColorVertex = UVColorVertexImpl(vector3, u, v, color)

fun uvColorVertex(
	vector3: Vector3<Number>,
	u: Float,
	v: Float,
	color: Color = Colors.WHITE
): UVColorVertex = UVColorVertexImpl(Vector3f(vector3), u, v, color)
