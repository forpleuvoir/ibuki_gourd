package com.forpleuvoir.ibukigourd.render.base.math

import com.forpleuvoir.nebula.common.notc
import com.forpleuvoir.nebula.serialization.Deserializable
import com.forpleuvoir.nebula.serialization.Serializable
import com.forpleuvoir.nebula.serialization.base.SerializeElement
import com.forpleuvoir.nebula.serialization.extensions.serializeObject

@Suppress("DuplicatedCode")
sealed interface Vector4<T : Number> : Serializable, Deserializable {

	var x: T
	var y: T
	var z: T
	var w: T

	override fun deserialization(serializeElement: SerializeElement) {
		if (!serializeElement.isObject) throw IllegalArgumentException("serializeElement${serializeElement} is not SerializeObject")
		serializeElement.asObject.apply {
			this.containsKey("x")
				.notc { throw IllegalArgumentException("serializeObject${serializeElement} is not have x") }
			this.containsKey("y")
				.notc { throw IllegalArgumentException("serializeObject${serializeElement} is not have y") }
			this.containsKey("z")
				.notc { throw IllegalArgumentException("serializeObject${serializeElement} is not have z") }
			this.containsKey("w")
				.notc { throw IllegalArgumentException("serializeObject${serializeElement} is not have w") }
			x = parseValue(this["x"]!!)
			y = parseValue(this["y"]!!)
			z = parseValue(this["z"]!!)
			w = parseValue(this["w"]!!)
		}

	}

	fun parseValue(serializeElement: SerializeElement): T

	override fun serialization(): SerializeElement {
		return serializeObject {
			"x" - x
			"y" - y
			"z" - z
			"w" - w
		}
	}

	fun set(x: T, y: T, z: T, w: T) {
		this.x = x
		this.y = y
		this.z = z
		this.w = w
	}

	fun set(vector4: Vector4<T>) {
		this.x = vector4.x
		this.y = vector4.y
		this.z = vector4.z
		this.w = vector4.w
	}

	fun x(x: T): Vector4<T>

	fun y(y: T): Vector4<T>

	fun z(z: T): Vector4<T>

	fun w(w: T): Vector4<T>

	fun xyzw(x: T = this.x, y: T = this.y, z: T = this.z, w: T = this.w): Vector4<T>

	operator fun plus(vector4: Vector4<T>): Vector4<T> {
		return plus(vector4.x, vector4.y, vector4.z, vector4.w)
	}

	fun plus(x: T, y: T, z: T, w: T): Vector4<T>


	operator fun plusAssign(vector4: Vector4<T>) {
		plusAssign(vector4.x, vector4.y, vector4.z, vector4.w)
	}

	fun plusAssign(x: T, y: T, z: T, w: T)

	operator fun minus(vector4: Vector4<T>): Vector4<T> {
		return minus(vector4.x, vector4.y, vector4.z, vector4.w)
	}

	fun minus(x: T, y: T, z: T, w: T): Vector4<T>

	operator fun minusAssign(vector4: Vector4<T>) {
		minusAssign(vector4.x, vector4.y, vector4.z, vector4.w)
	}

	fun minusAssign(x: T, y: T, z: T, w: T)

	operator fun unaryMinus(): Vector4<T>

	operator fun times(vector4: Vector4<T>): Vector4<T> {
		return times(vector4.x, vector4.y, vector4.z, vector4.w)
	}

	fun times(x: T, y: T, z: T, w: T): Vector4<T>

	operator fun timesAssign(vector4: Vector4<T>) {
		timesAssign(vector4.x, vector4.y, vector4.z, vector4.w)
	}

	fun timesAssign(x: T, y: T, z: T, w: T)

	operator fun div(vector4: Vector4<T>): Vector4<T> {
		return div(vector4.x, vector4.y, vector4.z, vector4.w)
	}

	fun div(x: T, y: T, z: T, w: T): Vector4<T>

	operator fun divAssign(vector4: Vector4<T>) {
		divAssign(vector4.x, vector4.y, vector4.z, vector4.w)
	}

	fun divAssign(x: T, y: T, z: T, w: T)

	operator fun rem(vector4: Vector4<T>): Vector4<T> {
		return rem(vector4.x, vector4.y, vector4.z, vector4.w)
	}

	fun rem(x: T, y: T, z: T, w: T): Vector4<T>

	operator fun remAssign(vector4: Vector4<T>) {
		remAssign(vector4.x, vector4.y, vector4.z, vector4.w)
	}

	fun remAssign(x: T, y: T, z: T, w: T)
}