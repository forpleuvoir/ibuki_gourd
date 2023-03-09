package com.forpleuvoir.ibukigourd.util.math

import com.forpleuvoir.nebula.serialization.base.SerializeElement

class Vector3i(
	override var x: Int = 0,
	override var y: Int = 0,
	override var z: Int = 0,
) : Vector3<Int> {

	constructor(vector3: Vector3<out Number>) : this(vector3.x.toInt(), vector3.y.toInt(), vector3.z.toInt())

	constructor(x: Number, y: Number, z: Number) : this(x.toInt(), y.toInt(), z.toInt())


	override fun valueMap(serializeElement: SerializeElement): Int {
		return serializeElement.asInt
	}

	override fun unaryMinus(): Vector3i {
		return Vector3i(-x, -y, -z)
	}

	override fun plus(x: Int, y: Int, z: Int): Vector3i {
		return Vector3i(this.x + x, this.y + y, this.z + z)
	}

	override fun plusAssign(x: Int, y: Int, z: Int) {
		this.x += x
		this.y += y
		this.z += z
	}

	override fun minus(x: Int, y: Int, z: Int): Vector3i {
		return Vector3i(this.x - x, this.y - y, this.z - z)
	}

	override fun minusAssign(x: Int, y: Int, z: Int) {
		this.x -= x
		this.y -= y
		this.z -= z
	}

	override fun times(x: Int, y: Int, z: Int): Vector3<Int> {
		return Vector3i(this.x * x, this.y * y, this.z * z)
	}

	override fun timesAssign(x: Int, y: Int, z: Int) {
		this.x *= x
		this.y *= y
		this.z *= z
	}

	override fun div(x: Int, y: Int, z: Int): Vector3<Int> {
		return Vector3i(this.x / x, this.y / y, this.z / z)
	}

	override fun divAssign(x: Int, y: Int, z: Int) {
		this.x /= x
		this.y /= y
		this.z /= z
	}

	override fun rem(x: Int, y: Int, z: Int): Vector3<Int> {
		return Vector3i(this.x % x, this.y % y, this.z % z)
	}

	override fun remAssign(x: Int, y: Int, z: Int) {
		this.x %= x
		this.y %= y
		this.z %= z
	}

}