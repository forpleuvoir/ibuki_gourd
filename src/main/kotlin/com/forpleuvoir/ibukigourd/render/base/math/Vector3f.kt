package com.forpleuvoir.ibukigourd.render.base.math

import com.forpleuvoir.nebula.serialization.base.SerializeElement

open class Vector3f(
	override var x: Float = 0f,
	override var y: Float = 0f,
	override var z: Float = 0f,
) : ImmutableVector3f(), MutableVector3<Float> {

	constructor(vector3: Vector3<out Number>) : this(vector3.x.toFloat(), vector3.y.toFloat(), vector3.z.toFloat())


	override fun parseValue(serializeElement: SerializeElement): Float {
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

	override fun plus(vector3: Vector3<Float>): Vector3f {
		return this.plus(vector3.x, vector3.y, vector3.z)
	}

	override fun minus(vector3: Vector3<Float>): Vector3f {
		return this.minus(vector3.x, vector3.y, vector3.z)
	}

	override fun times(vector3: Vector3<Float>): Vector3f {
		return this.times(vector3.x, vector3.y, vector3.z)
	}

	override fun div(vector3: Vector3<Float>): Vector3f {
		return this.div(vector3.x, vector3.y, vector3.z)
	}

	override fun rem(vector3: Vector3<Float>): Vector3f {
		return this.rem(vector3.x, vector3.y, vector3.z)
	}

	override fun plus(x: Float, y: Float, z: Float): Vector3f {
		return Vector3f(this.x + x, this.y + y, this.z + z)
	}

	override fun unaryPlus(): Vector3f {
		return Vector3f(+x, +y, +z)
	}

	override fun plusAssign(x: Float, y: Float, z: Float) {
		this.x += x
		this.y += y
		this.z += z
	}

	override fun minus(x: Float, y: Float, z: Float): Vector3f {
		return Vector3f(this.x - x, this.y - y, this.z - z)
	}

	override fun unaryMinus(): Vector3f {
		return Vector3f(-x, -y, -z)
	}

	override fun minusAssign(x: Float, y: Float, z: Float) {
		this.x -= x
		this.y -= y
		this.z -= z
	}

	override fun times(x: Float, y: Float, z: Float): Vector3f {
		return Vector3f(this.x * x, this.y * y, this.z * z)
	}

	override fun timesAssign(x: Float, y: Float, z: Float) {
		this.x *= x
		this.y *= y
		this.z *= z
	}

	override fun div(x: Float, y: Float, z: Float): Vector3f {
		return Vector3f(this.x / x, this.y / y, this.z / z)
	}

	override fun divAssign(x: Float, y: Float, z: Float) {
		this.x /= x
		this.y /= y
		this.z /= z
	}

	override fun rem(x: Float, y: Float, z: Float): Vector3f {
		return Vector3f(this.x % x, this.y % y, this.z % z)
	}

	override fun remAssign(x: Float, y: Float, z: Float) {
		this.x %= x
		this.y %= y
		this.z %= z
	}


	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Vector3f

		if (x != other.x) return false
		if (y != other.y) return false
		return z == other.z
	}

	override fun hashCode(): Int {
		var result = x.hashCode()
		result = 31 * result + y.hashCode()
		result = 31 * result + z.hashCode()
		return result
	}

}