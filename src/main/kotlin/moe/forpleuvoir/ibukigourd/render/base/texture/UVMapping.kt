package moe.forpleuvoir.ibukigourd.render.base.texture

import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.extensions.getOr

open class UVMapping(
	val u1: Int,
	val v1: Int,
	val u2: Int,
	val v2: Int,
) {

	companion object {
		fun uv(u: Int, v: Int, uSize: Int, vSize: Int) =
			UVMapping(u, v, u + uSize, v + vSize)


		fun deserialization(serializeElement: SerializeElement?, default: UVMapping): UVMapping {
			if (serializeElement == null || !serializeElement.isObject) return default
			serializeElement.asObject.apply {
				return try {
					val u1: Int
					val u2: Int
					if (containsKey("u") && containsKey("u_size")) {
						u1 = get("u")!!.asInt
						u2 = get("u_size")!!.asInt + u1
					} else if (containsKey("u") && containsKey("width")) {
						u1 = get("u")!!.asInt
						u2 = get("width")!!.asInt + u1
					} else {
						u1 = getOr("u1", default.u1).toInt()
						u2 = getOr("u2", default.u2).toInt()
					}
					val v1: Int
					val v2: Int
					if (containsKey("v") && containsKey("v_size")) {
						v1 = get("v")!!.asInt
						v2 = get("v_size")!!.asInt + v1
					} else if (containsKey("v") && containsKey("height")) {
						v1 = get("u")!!.asInt
						v2 = get("height")!!.asInt + v1
					} else {
						v1 = getOr("v1", default.v1).toInt()
						v2 = getOr("v2", default.v2).toInt()
					}
					UVMapping(u1, v1, u2, v2)
				} catch (_: Exception) {
					default
				}
			}
		}
	}

	val uSize = u2 - u1

	val vSize = v2 - v1


	override fun toString(): String {
		return "UVMapping(u1=$u1, v1=$v1, u2=$u2, v2=$v2)"
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as UVMapping

		if (u1 != other.u1) return false
		if (v1 != other.v1) return false
		if (u2 != other.u2) return false
		return v2 == other.v2
	}

	override fun hashCode(): Int {
		var result = u1
		result = 31 * result + v1
		result = 31 * result + u2
		result = 31 * result + v2
		return result
	}


}