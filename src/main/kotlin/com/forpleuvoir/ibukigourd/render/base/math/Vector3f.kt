package com.forpleuvoir.ibukigourd.render.base.math

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

	override fun x(x: Float): Vector3f {
		return Vector3f(x, this.y, this.z)
	}

	override fun y(y: Float): Vector3f {
		return Vector3f(this.x, y, this.z)
	}

	override fun z(z: Float): Vector3f {
		return Vector3f(this.x, this.y, z)
	}

	override fun xyz(x: Float, y: Float, z: Float): Vector3f {
		return Vector3f(x, y, z)
	}

	override fun unaryMinus(): Vector3f {
		return Vector3f(-x, -y, -z)
	}

	override operator fun plus(vector3: Vector3<Float>): Vector3f {
		return plus(vector3.x, vector3.y, vector3.z)
	}

	override fun plus(x: Float, y: Float, z: Float): Vector3f {
		return Vector3f(this.x + x, this.y + y, this.z + z)
	}

	override fun plusAssign(x: Float, y: Float, z: Float) {
		this.x += x
		this.y += y
		this.z += z
	}

	override operator fun minus(vector3: Vector3<Float>): Vector3f {
		return minus(vector3.x, vector3.y, vector3.z)
	}

	override fun minus(x: Float, y: Float, z: Float): Vector3f {
		return Vector3f(this.x - x, this.y - y, this.z - z)
	}

	override fun minusAssign(x: Float, y: Float, z: Float) {
		this.x -= x
		this.y -= y
		this.z -= z
	}

	override operator fun times(vector3: Vector3<Float>): Vector3f {
		return times(vector3.x, vector3.y, vector3.z)
	}

	override fun times(x: Float, y: Float, z: Float): Vector3f {
		return Vector3f(this.x * x, this.y * y, this.z * z)
	}

	override fun timesAssign(x: Float, y: Float, z: Float) {
		this.x *= x
		this.y *= y
		this.z *= z
	}

	override operator fun div(vector3: Vector3<Float>): Vector3f {
		return div(vector3.x, vector3.y, vector3.z)
	}

	override fun div(x: Float, y: Float, z: Float): Vector3f {
		return Vector3f(this.x / x, this.y / y, this.z / z)
	}

	override fun divAssign(x: Float, y: Float, z: Float) {
		this.x /= x
		this.y /= y
		this.z /= z
	}

	override operator fun rem(vector3: Vector3<Float>): Vector3f {
		return rem(vector3.x, vector3.y, vector3.z)
	}


	override fun rem(x: Float, y: Float, z: Float): Vector3f {
		return Vector3f(this.x % x, this.y % y, this.z % z)
	}

	override fun remAssign(x: Float, y: Float, z: Float) {
		this.x %= x
		this.y %= y
		this.z %= z
	}

}