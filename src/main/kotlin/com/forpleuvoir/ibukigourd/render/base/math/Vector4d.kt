package com.forpleuvoir.ibukigourd.render.base.math

import com.forpleuvoir.nebula.serialization.base.SerializeElement

class Vector4d(
	override var x: Double = 0.0,
	override var y: Double = 0.0,
	override var z: Double = 0.0,
	override var w: Double = 0.0,
) : Vector4<Double> {

	constructor(vector4: Vector4<out Number>) : this(vector4.x.toDouble(), vector4.y.toDouble(), vector4.z.toDouble(), vector4.w.toDouble())

	constructor(x: Number, y: Number, z: Number, w: Number) : this(x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble())

	override fun parseValue(serializeElement: SerializeElement): Double {
		return serializeElement.asDouble
	}

	override fun x(x: Double): Vector4d {
		return Vector4d(x, this.y, this.z, this.w)
	}

	override fun y(y: Double): Vector4d {
		return Vector4d(this.x, y, this.z, this.w)
	}

	override fun z(z: Double): Vector4d {
		return Vector4d(this.x, this.y, z, this.w)
	}

	override fun w(w: Double): Vector4d {
		return Vector4d(this.x, this.y, this.z, w)
	}

	override fun xyzw(x: Double, y: Double, z: Double, w: Double): Vector4d {
		return Vector4d(x, y, z, w)
	}

	override fun unaryMinus(): Vector4d {
		return Vector4d(-x, -y, -z)
	}

	override fun plus(x: Double, y: Double, z: Double, w: Double): Vector4d {
		return Vector4d(this.x + x, this.y + y, this.z + z, this.w + w)
	}

	override fun plusAssign(x: Double, y: Double, z: Double, w: Double) {
		this.x += x
		this.y += y
		this.z += z
		this.w += w
	}

	override fun minus(x: Double, y: Double, z: Double, w: Double): Vector4d {
		return Vector4d(this.x - x, this.y - y, this.z - z, this.w - w)
	}

	override fun minusAssign(x: Double, y: Double, z: Double, w: Double) {
		this.x -= x
		this.y -= y
		this.z -= z
		this.w -= w
	}

	override fun times(x: Double, y: Double, z: Double, w: Double): Vector4<Double> {
		return Vector4d(this.x * x, this.y * y, this.z * z, this.w * w)
	}

	override fun timesAssign(x: Double, y: Double, z: Double, w: Double) {
		this.x *= x
		this.y *= y
		this.z *= z
	}

	override fun div(x: Double, y: Double, z: Double, w: Double): Vector4<Double> {
		return Vector4d(this.x / x, this.y / y, this.z / z, this.w / w)
	}

	override fun divAssign(x: Double, y: Double, z: Double, w: Double) {
		this.x /= x
		this.y /= y
		this.z /= z
		this.w /= w
	}

	override fun rem(x: Double, y: Double, z: Double, w: Double): Vector4<Double> {
		return Vector4d(this.x % x, this.y % y, this.z % z, this.w % w)
	}

	override fun remAssign(x: Double, y: Double, z: Double, w: Double) {
		this.x %= x
		this.y %= y
		this.z %= z
		this.w %= w
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Vector4d

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