package com.forpleuvoir.ibukigourd.render.base.vertex

import com.forpleuvoir.ibukigourd.render.base.math.Vector3f
import com.forpleuvoir.nebula.common.color.Color
import com.forpleuvoir.nebula.common.color.Colors

class UVColorVertexImpl(
	vector3: Vector3f,
	override val u: Float,
	override val v: Float,
	override val color: Color = Colors.WHITE
) : UVColorVertex {

	override val x: Float = vector3.x

	override val y: Float = vector3.y

	override val z: Float = vector3.z
	override fun x(x: Number): UVColorVertex {
		return UVColorVertexImpl(vector3f().x(x.toFloat()), u, v, color)
	}

	override fun y(y: Number): UVColorVertex {
		return UVColorVertexImpl(vector3f().y(y.toFloat()), u, v, color)
	}

	override fun z(z: Number): UVColorVertex {
		return UVColorVertexImpl(vector3f().z(z.toFloat()), u, v, color)
	}

	override fun xyz(x: Number, y: Number, z: Number): UVColorVertex {
		return UVColorVertexImpl(vector3f().xyz(x.toFloat(), y.toFloat(), z.toFloat()), u, v, color)
	}

	override fun u(u: Float): UVColorVertex {
		return UVColorVertexImpl(vector3f(), u, v, color)
	}

	override fun v(v: Float): UVColorVertex {
		return UVColorVertexImpl(vector3f(), u, v, color)
	}

	override fun uv(u: Float, v: Float): UVColorVertex {
		return UVColorVertexImpl(vector3f(), u, v, color)
	}

	override fun color(color: Color): UVColorVertex {
		return UVColorVertexImpl(vector3f(), u, v, color)
	}

}