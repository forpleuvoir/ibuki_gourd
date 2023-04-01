package com.forpleuvoir.ibukigourd.render.base.math


import com.forpleuvoir.nebula.common.notc
import com.forpleuvoir.nebula.serialization.Deserializable
import com.forpleuvoir.nebula.serialization.Serializable
import com.forpleuvoir.nebula.serialization.base.SerializeElement
import com.forpleuvoir.nebula.serialization.extensions.serializeObject


sealed interface Vector3<T : Number> : Serializable, Deserializable {

	var x: T
	var y: T
	var z: T

	override fun deserialization(serializeElement: SerializeElement) {
		if (!serializeElement.isObject) throw IllegalArgumentException("serializeElement${serializeElement} is not SerializeObject")
		serializeElement.asObject.apply {
			this.containsKey("x")
				.notc { throw IllegalArgumentException("serializeObject${serializeElement} is not have x") }
			this.containsKey("y")
				.notc { throw IllegalArgumentException("serializeObject${serializeElement} is not have y") }
			this.containsKey("z")
				.notc { throw IllegalArgumentException("serializeObject${serializeElement} is not have z") }
			x = valueMap(this["x"]!!)
			y = valueMap(this["y"]!!)
			z = valueMap(this["z"]!!)
		}

	}

	fun valueMap(serializeElement: SerializeElement): T

	override fun serialization(): SerializeElement {
		return serializeObject {
			"x" - x
			"y" - y
			"z" - z
		}
	}

	fun set(x: T, y: T, z: T) {
		this.x = x
		this.y = y
		this.z = z
	}

	fun set(vector3: Vector3<T>) {
		this.x = vector3.x
		this.y = vector3.y
		this.z = vector3.z
	}

	fun x(x: T): Vector3<T>

	fun y(y: T): Vector3<T>

	fun z(z: T): Vector3<T>

	fun xyz(x: T = this.x, y: T = this.y, z: T = this.z): Vector3<T>

	operator fun plus(vector3: Vector3<T>): Vector3<T> {
		return plus(vector3.x, vector3.y, vector3.z)
	}

	fun plus(x: T, y: T, z: T): Vector3<T>


	operator fun plusAssign(vector3: Vector3<T>) {
		plusAssign(vector3.x, vector3.y, vector3.z)
	}

	fun plusAssign(x: T, y: T, z: T)

	operator fun minus(vector3: Vector3<T>): Vector3<T> {
		return minus(vector3.x, vector3.y, vector3.z)
	}

	fun minus(x: T, y: T, z: T): Vector3<T>

	operator fun minusAssign(vector3: Vector3<T>) {
		minusAssign(vector3.x, vector3.y, vector3.z)
	}

	fun minusAssign(x: T, y: T, z: T)

	operator fun unaryMinus(): Vector3<T>

	operator fun times(vector3: Vector3<T>): Vector3<T> {
		return times(vector3.x, vector3.y, vector3.z)
	}

	fun times(x: T, y: T, z: T): Vector3<T>

	operator fun timesAssign(vector3: Vector3<T>) {
		timesAssign(vector3.x, vector3.y, vector3.z)
	}

	fun timesAssign(x: T, y: T, z: T)

	operator fun div(vector3: Vector3<T>): Vector3<T> {
		return div(vector3.x, vector3.y, vector3.z)
	}

	fun div(x: T, y: T, z: T): Vector3<T>

	operator fun divAssign(vector3: Vector3<T>) {
		divAssign(vector3.x, vector3.y, vector3.z)
	}

	fun divAssign(x: T, y: T, z: T)

	operator fun rem(vector3: Vector3<T>): Vector3<T> {
		return rem(vector3.x, vector3.y, vector3.z)
	}

	fun rem(x: T, y: T, z: T): Vector3<T>

	operator fun remAssign(vector3: Vector3<T>) {
		remAssign(vector3.x, vector3.y, vector3.z)
	}

	fun remAssign(x: T, y: T, z: T)

}