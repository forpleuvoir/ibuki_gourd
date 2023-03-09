package com.forpleuvoir.ibukigourd.util.math

import com.forpleuvoir.nebula.serialization.base.SerializeElement

class Vector3f(
	override var x: Float = 0f,
	override var y: Float = 0f,
	override var z: Float = 0f,
) : Vector3<Float> {

	constructor(vector3: Vector3<out Number>) : this(vector3.x.toFloat(), vector3.y.toFloat(), vector3.z.toFloat())

	constructor(x: Number, y: Number, z: Number) : this(x.toFloat(), y.toFloat(), z.toFloat())

	override fun valueMap(serializeElement: SerializeElement): Float {
		return serializeElement.asFloat
	}

	override fun unaryMinus(): Vector3f {
		return Vector3f(-x, -y, -z)
	}

	override fun plus(x: Float, y: Float, z: Float): Vector3f {
		return Vector3f(this.x + x, this.y + y, this.z + z)
	}

	override fun plusAssign(x: Float, y: Float, z: Float) {
		this.x += x
		this.y += y
		this.z += z
	}

	override fun minus(x: Float, y: Float, z: Float): Vector3f {
		return Vector3f(this.x - x, this.y - y, this.z - z)
	}

	override fun minusAssign(x: Float, y: Float, z: Float) {
		this.x -= x
		this.y -= y
		this.z -= z
	}

	override fun times(x: Float, y: Float, z: Float): Vector3<Float> {
		return Vector3f(this.x * x, this.y * y, this.z * z)
	}

	override fun timesAssign(x: Float, y: Float, z: Float) {
		this.x *= x
		this.y *= y
		this.z *= z
	}

	override fun div(x: Float, y: Float, z: Float): Vector3<Float> {
		return Vector3f(this.x / x, this.y / y, this.z / z)
	}

	override fun divAssign(x: Float, y: Float, z: Float) {
		this.x /= x
		this.y /= y
		this.z /= z
	}

	override fun rem(x: Float, y: Float, z: Float): Vector3<Float> {
		return Vector3f(this.x % x, this.y % y, this.z % z)
	}

	override fun remAssign(x: Float, y: Float, z: Float) {
		this.x %= x
		this.y %= y
		this.z %= z
	}

}