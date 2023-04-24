package com.forpleuvoir.ibukigourd.render.base.math

import com.forpleuvoir.nebula.serialization.base.SerializeElement

class Vector4f(
	override var x: Float = 0.0f,
	override var y: Float = 0.0f,
	override var z: Float = 0.0f,
	override var w: Float = 0.0f,
) : Vector4<Float> {

	constructor(vector4: Vector4<out Number>) : this(vector4.x.toFloat(), vector4.y.toFloat(), vector4.z.toFloat(), vector4.w.toFloat())

	constructor(x: Number, y: Number, z: Number, w: Number) : this(x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat())

	override fun parseValue(serializeElement: SerializeElement): Float {
		return serializeElement.asFloat
	}

	override fun x(x: Float): Vector4f {
		return Vector4f(x, this.y, this.z, this.w)
	}

	override fun y(y: Float): Vector4f {
		return Vector4f(this.x, y, this.z, this.w)
	}

	override fun z(z: Float): Vector4f {
		return Vector4f(this.x, this.y, z, this.w)
	}

	override fun w(w: Float): Vector4f {
		return Vector4f(this.x, this.y, this.z, w)
	}

	override fun xyzw(x: Float, y: Float, z: Float, w: Float): Vector4f {
		return Vector4f(x, y, z, w)
	}

	override fun unaryMinus(): Vector4f {
		return Vector4f(-x, -y, -z)
	}

	override fun plus(x: Float, y: Float, z: Float, w: Float): Vector4f {
		return Vector4f(this.x + x, this.y + y, this.z + z, this.w + w)
	}

	override fun plusAssign(x: Float, y: Float, z: Float, w: Float) {
		this.x += x
		this.y += y
		this.z += z
		this.w += w
	}

	override fun minus(x: Float, y: Float, z: Float, w: Float): Vector4f {
		return Vector4f(this.x - x, this.y - y, this.z - z, this.w - w)
	}

	override fun minusAssign(x: Float, y: Float, z: Float, w: Float) {
		this.x -= x
		this.y -= y
		this.z -= z
		this.w -= w
	}

	override fun times(x: Float, y: Float, z: Float, w: Float): Vector4<Float> {
		return Vector4f(this.x * x, this.y * y, this.z * z, this.w * w)
	}

	override fun timesAssign(x: Float, y: Float, z: Float, w: Float) {
		this.x *= x
		this.y *= y
		this.z *= z
	}

	override fun div(x: Float, y: Float, z: Float, w: Float): Vector4<Float> {
		return Vector4f(this.x / x, this.y / y, this.z / z, this.w / w)
	}

	override fun divAssign(x: Float, y: Float, z: Float, w: Float) {
		this.x /= x
		this.y /= y
		this.z /= z
		this.w /= w
	}

	override fun rem(x: Float, y: Float, z: Float, w: Float): Vector4<Float> {
		return Vector4f(this.x % x, this.y % y, this.z % z, this.w % w)
	}

	override fun remAssign(x: Float, y: Float, z: Float, w: Float) {
		this.x %= x
		this.y %= y
		this.z %= z
		this.w %= w
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Vector4f

		if (x != other.x) return false
		if (y != other.y) return false
		if (z != other.z) return false
		return w == other.w
	}

	override fun hashCode(): Int {
		var result = x.hashCode()
		result = 31 * result + y.hashCode()
		result = 31 * result + z.hashCode()
		result = 31 * result + w.hashCode()
		return result
	}

}