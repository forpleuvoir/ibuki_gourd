package moe.forpleuvoir.ibukigourd.render.base.math


import moe.forpleuvoir.nebula.common.notc
import moe.forpleuvoir.nebula.serialization.Deserializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement

@Suppress("DuplicatedCode")
interface MutableVector3<T : Number> : Vector3<T>, Deserializable {

	override var x: T
	override var y: T
	override var z: T

	var changeCallback: ((x: T, y: T, z: T) -> Unit)?

	override fun deserialization(serializeElement: SerializeElement) {
		if (!serializeElement.isObject) throw IllegalArgumentException("serializeElement${serializeElement} is not SerializeObject")
		serializeElement.asObject.apply {
			this.containsKey("x")
				.notc { throw IllegalArgumentException("serializeObject${serializeElement} is not have x") }
			this.containsKey("y")
				.notc { throw IllegalArgumentException("serializeObject${serializeElement} is not have y") }
			this.containsKey("z")
				.notc { throw IllegalArgumentException("serializeObject${serializeElement} is not have z") }
			x = parseValue(this["x"]!!)
			y = parseValue(this["y"]!!)
			z = parseValue(this["z"]!!)
		}

	}

	fun parseValue(serializeElement: SerializeElement): T

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

	operator fun plusAssign(vector3: Vector3<T>) {
		plusAssign(vector3.x, vector3.y, vector3.z)
	}

	fun plusAssign(x: T, y: T, z: T)

	operator fun minusAssign(vector3: Vector3<T>) {
		minusAssign(vector3.x, vector3.y, vector3.z)
	}

	fun minusAssign(x: T, y: T, z: T)

	operator fun timesAssign(vector3: Vector3<T>) {
		timesAssign(vector3.x, vector3.y, vector3.z)
	}

	fun timesAssign(x: T, y: T, z: T)

	operator fun divAssign(vector3: Vector3<T>) {
		divAssign(vector3.x, vector3.y, vector3.z)
	}

	fun divAssign(x: T, y: T, z: T)

	operator fun remAssign(vector3: Vector3<T>) {
		remAssign(vector3.x, vector3.y, vector3.z)
	}

	fun remAssign(x: T, y: T, z: T)

}