package com.forpleuvoir.ibukigourd.render.base.math

import com.forpleuvoir.nebula.serialization.base.SerializeElement

class Vector3d(
	override var x: Double = 0.0,
	override var y: Double = 0.0,
	override var z: Double = 0.0,
) : Vector3<Double> {

	constructor(vector3: Vector3<out Number>) : this(vector3.x.toDouble(), vector3.y.toDouble(), vector3.z.toDouble())

	constructor(x: Number, y: Number, z: Number) : this(x.toDouble(), y.toDouble(), z.toDouble())

	override fun valueMap(serializeElement: SerializeElement): Double {
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

	override fun unaryMinus(): Vector3d {
		return Vector3d(-x, -y, -z)
	}

	override fun plus(x: Double, y: Double, z: Double): Vector3d {
		return Vector3d(this.x + x, this.y + y, this.z + z)
	}

	override fun plusAssign(x: Double, y: Double, z: Double) {
		this.x += x
		this.y += y
		this.z += z
	}

	override fun minus(x: Double, y: Double, z: Double): Vector3d {
		return Vector3d(this.x - x, this.y - y, this.z - z)
	}

	override fun minusAssign(x: Double, y: Double, z: Double) {
		this.x -= x
		this.y -= y
		this.z -= z
	}

	override fun times(x: Double, y: Double, z: Double): Vector3<Double> {
		return Vector3d(this.x * x, this.y * y, this.z * z)
	}

	override fun timesAssign(x: Double, y: Double, z: Double) {
		this.x *= x
		this.y *= y
		this.z *= z
	}

	override fun div(x: Double, y: Double, z: Double): Vector3<Double> {
		return Vector3d(this.x / x, this.y / y, this.z / z)
	}

	override fun divAssign(x: Double, y: Double, z: Double) {
		this.x /= x
		this.y /= y
		this.z /= z
	}

	override fun rem(x: Double, y: Double, z: Double): Vector3<Double> {
		return Vector3d(this.x % x, this.y % y, this.z % z)
	}

	override fun remAssign(x: Double, y: Double, z: Double) {
		this.x %= x
		this.y %= y
		this.z %= z
	}

}