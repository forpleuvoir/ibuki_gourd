package com.forpleuvoir.ibukigourd.render.base.math

import com.forpleuvoir.nebula.serialization.base.SerializeElement

class Vector4i(
	override var x: Int = 0,
	override var y: Int = 0,
	override var z: Int = 0,
	override var w: Int = 0,
) : Vector4<Int> {

	constructor(vector4: Vector4<out Number>) : this(vector4.x.toInt(), vector4.y.toInt(), vector4.z.toInt(), vector4.w.toInt())

	constructor(x: Number, y: Number, z: Number, w: Number) : this(x.toInt(), y.toInt(), z.toInt(), w.toInt())

	override fun parseValue(serializeElement: SerializeElement): Int {
		return serializeElement.asInt
	}

	override fun x(x: Int): Vector4i {
		return Vector4i(x, this.y, this.z, this.w)
	}

	override fun y(y: Int): Vector4i {
		return Vector4i(this.x, y, this.z, this.w)
	}

	override fun z(z: Int): Vector4i {
		return Vector4i(this.x, this.y, z, this.w)
	}

	override fun w(w: Int): Vector4i {
		return Vector4i(this.x, this.y, this.z, w)
	}

	override fun xyzw(x: Int, y: Int, z: Int, w: Int): Vector4i {
		return Vector4i(x, y, z, w)
	}

	override fun unaryMinus(): Vector4i {
		return Vector4i(-x, -y, -z)
	}

	override fun plus(x: Int, y: Int, z: Int, w: Int): Vector4i {
		return Vector4i(this.x + x, this.y + y, this.z + z, this.w + w)
	}

	override fun plusAssign(x: Int, y: Int, z: Int, w: Int) {
		this.x += x
		this.y += y
		this.z += z
		this.w += w
	}

	override fun minus(x: Int, y: Int, z: Int, w: Int): Vector4i {
		return Vector4i(this.x - x, this.y - y, this.z - z, this.w - w)
	}

	override fun minusAssign(x: Int, y: Int, z: Int, w: Int) {
		this.x -= x
		this.y -= y
		this.z -= z
		this.w -= w
	}

	override fun times(x: Int, y: Int, z: Int, w: Int): Vector4<Int> {
		return Vector4i(this.x * x, this.y * y, this.z * z, this.w * w)
	}

	override fun timesAssign(x: Int, y: Int, z: Int, w: Int) {
		this.x *= x
		this.y *= y
		this.z *= z
	}

	override fun div(x: Int, y: Int, z: Int, w: Int): Vector4<Int> {
		return Vector4i(this.x / x, this.y / y, this.z / z, this.w / w)
	}

	override fun divAssign(x: Int, y: Int, z: Int, w: Int) {
		this.x /= x
		this.y /= y
		this.z /= z
		this.w /= w
	}

	override fun rem(x: Int, y: Int, z: Int, w: Int): Vector4<Int> {
		return Vector4i(this.x % x, this.y % y, this.z % z, this.w % w)
	}

	override fun remAssign(x: Int, y: Int, z: Int, w: Int) {
		this.x %= x
		this.y %= y
		this.z %= z
		this.w %= w
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Vector4i

		if (x != other.x) return false
		if (y != other.y) return false
		if (z != other.z) return false
		return w == other.w
	}

	override fun hashCode(): Int {
		var result = x
		result = 31 * result + y
		result = 31 * result + z
		result = 31 * result + w
		return result
	}


}