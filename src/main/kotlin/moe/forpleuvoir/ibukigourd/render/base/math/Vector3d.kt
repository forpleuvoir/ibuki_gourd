package moe.forpleuvoir.ibukigourd.render.base.math

import moe.forpleuvoir.nebula.serialization.base.SerializeElement

open class Vector3d(
	x: Double = 0.0,
	y: Double = 0.0,
	z: Double = 0.0,
) : ImmutableVector3d(), MutableVector3<Double> {

	override var x: Double = x
		set(value) {
			if (value != x) {
				changeCallback?.invoke(value, y, z)
			}
			field = value
		}
	override var y: Double = y
		set(value) {
			if (value != y) {
				changeCallback?.invoke(x, value, z)
			}
			field = value
		}
	override var z: Double = z
		set(value) {
			if (value != z) {
				changeCallback?.invoke(x, y, value)
			}
			field = value
		}

	override var changeCallback: ((x: Double, y: Double, z: Double) -> Unit)? = null

	constructor(vector3: Vector3<out Number>) : this(vector3.x.toDouble(), vector3.y.toDouble(), vector3.z.toDouble())

	constructor(x: Number, y: Number, z: Number) : this(x.toDouble(), y.toDouble(), z.toDouble())

	override fun parseValue(serializeElement: SerializeElement): Double {
		return serializeElement.asDouble
	}

	override fun x(x: Double): Vector3d {
		return Vector3d(x, this.y, this.z)
	}

	override fun y(y: Double): Vector3d {
		return Vector3d(this.x, y, this.z)
	}

	override fun z(z: Double): Vector3d {
		return Vector3d(this.x, this.y, z)
	}

	override fun xyz(x: Double, y: Double, z: Double): Vector3d {
		return Vector3d(x, y, z)
	}

	override fun plus(vector3: Vector3<Double>): Vector3d {
		return this.plus(vector3.x, vector3.y, vector3.z)
	}

	override fun minus(vector3: Vector3<Double>): Vector3d {
		return this.minus(vector3.x, vector3.y, vector3.z)
	}

	override fun times(vector3: Vector3<Double>): Vector3d {
		return this.times(vector3.x, vector3.y, vector3.z)
	}

	override fun div(vector3: Vector3<Double>): Vector3d {
		return this.div(vector3.x, vector3.y, vector3.z)
	}

	override fun rem(vector3: Vector3<Double>): Vector3d {
		return this.rem(vector3.x, vector3.y, vector3.z)
	}

	override fun plus(x: Double, y: Double, z: Double): Vector3d {
		return Vector3d(this.x + x, this.y + y, this.z + z)
	}

	override fun unaryPlus(): Vector3d {
		return Vector3d(+x, +y, +z)
	}

	override fun plusAssign(x: Double, y: Double, z: Double) {
		this.x += x
		this.y += y
		this.z += z
	}

	override fun minus(x: Double, y: Double, z: Double): Vector3d {
		return Vector3d(this.x - x, this.y - y, this.z - z)
	}

	override fun unaryMinus(): Vector3d {
		return Vector3d(-x, -y, -z)
	}

	override fun minusAssign(x: Double, y: Double, z: Double) {
		this.x -= x
		this.y -= y
		this.z -= z
	}

	override fun times(x: Double, y: Double, z: Double): Vector3d {
		return Vector3d(this.x * x, this.y * y, this.z * z)
	}

	override fun timesAssign(x: Double, y: Double, z: Double) {
		this.x *= x
		this.y *= y
		this.z *= z
	}

	override fun div(x: Double, y: Double, z: Double): Vector3d {
		return Vector3d(this.x / x, this.y / y, this.z / z)
	}

	override fun divAssign(x: Double, y: Double, z: Double) {
		this.x /= x
		this.y /= y
		this.z /= z
	}

	override fun rem(x: Double, y: Double, z: Double): Vector3d {
		return Vector3d(this.x % x, this.y % y, this.z % z)
	}

	override fun remAssign(x: Double, y: Double, z: Double) {
		this.x %= x
		this.y %= y
		this.z %= z
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Vector3<*>

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