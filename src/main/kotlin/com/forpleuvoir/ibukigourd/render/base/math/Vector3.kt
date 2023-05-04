package com.forpleuvoir.ibukigourd.render.base.math


import com.forpleuvoir.nebula.serialization.Serializable
import com.forpleuvoir.nebula.serialization.base.SerializeElement
import com.forpleuvoir.nebula.serialization.extensions.serializeObject

@Suppress("DuplicatedCode")
interface Vector3<T : Number> : Serializable {

	companion object {
		fun <T : Number> equals(vector3: Vector3<T>, other: Vector3<T>): Boolean {
			if (vector3.x != other.x) return false
			if (vector3.y != other.y) return false
			return vector3.z == other.z
		}
	}

	val x: T
	val y: T
	val z: T

	override fun serialization(): SerializeElement {
		return serializeObject {
			"x" - x
			"y" - y
			"z" - z
		}
	}

	fun x(x: T): Vector3<T>

	fun y(y: T): Vector3<T>

	fun z(z: T): Vector3<T>

	fun xyz(x: T = this.x, y: T = this.y, z: T = this.z): Vector3<T>

	operator fun plus(vector3: Vector3<T>): Vector3<T> {
		return plus(vector3.x, vector3.y, vector3.z)
	}

	fun plus(x: T, y: T, z: T): Vector3<T>

	operator fun unaryPlus(): Vector3<T>

	operator fun minus(vector3: Vector3<T>): Vector3<T> {
		return minus(vector3.x, vector3.y, vector3.z)
	}

	fun minus(x: T, y: T, z: T): Vector3<T>

	operator fun unaryMinus(): Vector3<T>

	operator fun times(vector3: Vector3<T>): Vector3<T> {
		return times(vector3.x, vector3.y, vector3.z)
	}

	fun times(x: T, y: T, z: T): Vector3<T>

	operator fun div(vector3: Vector3<T>): Vector3<T> {
		return div(vector3.x, vector3.y, vector3.z)
	}

	fun div(x: T, y: T, z: T): Vector3<T>

	operator fun rem(vector3: Vector3<T>): Vector3<T> {
		return rem(vector3.x, vector3.y, vector3.z)
	}

	fun rem(x: T, y: T, z: T): Vector3<T>
}

