package moe.forpleuvoir.ibukigourd.render.base.math

import moe.forpleuvoir.nebula.serialization.base.SerializeElement

open class Vector3i(
	x: Int = 0,
	y: Int = 0,
	z: Int = 0,
) : ImmutableVector3i(x, y, z), MutableVector3<Int> {

	override var x: Int = x
		set(value) {
			if (value != x) {
				changeCallback?.invoke(value, y, z)
			}
			field = value
		}
	override var y: Int = y
		set(value) {
			if (value != y) {
				changeCallback?.invoke(x, value, z)
			}
			field = value
		}
	override var z: Int = z
		set(value) {
			if (value != z) {
				changeCallback?.invoke(x, y, value)
			}
			field = value
		}

	override var changeCallback: ((x: Int, y: Int, z: Int) -> Unit)? = null

	constructor(vector3: Vector3<out Number>) : this(vector3.x.toInt(), vector3.y.toInt(), vector3.z.toInt())

	override fun parseValue(serializeElement: SerializeElement): Int {
		return serializeElement.asInt
	}

	override fun x(x: Int): Vector3i {
		return Vector3i(x, this.y, this.z)
	}

	override fun y(y: Int): Vector3i {
		return Vector3i(this.x, y, this.z)
	}

	override fun z(z: Int): Vector3i {
		return Vector3i(this.x, this.y, z)
	}

	override fun xyz(x: Int, y: Int, z: Int): Vector3i {
		return Vector3i(x, y, z)
	}

	override fun plus(vector3: Vector3<Int>): Vector3i {
		return this.plus(vector3.x, vector3.y, vector3.z)
	}

	override fun minus(vector3: Vector3<Int>): Vector3i {
		return this.minus(vector3.x, vector3.y, vector3.z)
	}

	override fun times(vector3: Vector3<Int>): Vector3i {
		return this.times(vector3.x, vector3.y, vector3.z)
	}

	override fun div(vector3: Vector3<Int>): Vector3i {
		return this.div(vector3.x, vector3.y, vector3.z)
	}

	override fun rem(vector3: Vector3<Int>): Vector3i {
		return this.rem(vector3.x, vector3.y, vector3.z)
	}

	override fun plus(x: Int, y: Int, z: Int): Vector3i {
		return Vector3i(this.x + x, this.y + y, this.z + z)
	}


	override fun unaryPlus(): Vector3i {
		return Vector3i(+x, +y, +z)
	}

	override fun plusAssign(x: Int, y: Int, z: Int) {
		this.x += x
		this.y += y
		this.z += z
	}

	override fun minus(x: Int, y: Int, z: Int): Vector3i {
		return Vector3i(this.x - x, this.y - y, this.z - z)
	}

	override fun unaryMinus(): Vector3i {
		return Vector3i(-x, -y, -z)
	}

	override fun minusAssign(x: Int, y: Int, z: Int) {
		this.x -= x
		this.y -= y
		this.z -= z
	}

	override fun times(x: Int, y: Int, z: Int): Vector3i {
		return Vector3i(this.x * x, this.y * y, this.z * z)
	}

	override fun timesAssign(x: Int, y: Int, z: Int) {
		this.x *= x
		this.y *= y
		this.z *= z
	}

	override fun div(x: Int, y: Int, z: Int): Vector3i {
		return Vector3i(this.x / x, this.y / y, this.z / z)
	}

	override fun divAssign(x: Int, y: Int, z: Int) {
		this.x /= x
		this.y /= y
		this.z /= z
	}

	override fun rem(x: Int, y: Int, z: Int): Vector3i {
		return Vector3i(this.x % x, this.y % y, this.z % z)
	}

	override fun remAssign(x: Int, y: Int, z: Int) {
		this.x %= x
		this.y %= y
		this.z %= z
	}

}