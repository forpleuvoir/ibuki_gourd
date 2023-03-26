package com.forpleuvoir.ibukigourd.render.base.texture

import com.forpleuvoir.nebula.serialization.json.getOr
import com.google.gson.JsonObject

open class UVMapping(
	val u1: Int,
	val v1: Int,
	val u2: Int,
	val v2: Int,
) {

	companion object {
		fun uv(u: Int, v: Int, uSize: Int, vSize: Int) =
			UVMapping(u, v, u + uSize, v + vSize)


		fun fromJson(jsonObject: JsonObject, default: UVMapping): UVMapping {
			jsonObject.apply {
				return try {
					val u1: Int
					val u2: Int
					if (has("u") && has("u_size")) {
						u1 = get("u").asInt
						u2 = get("u_size").asInt + u1
					} else if (has("u") && has("width")) {
						u1 = get("u").asInt
						u2 = get("width").asInt + u1
					} else {
						u1 = getOr("u1", default.u1).toInt()
						u2 = getOr("u2", default.u2).toInt()
					}
					val v1: Int
					val v2: Int
					if (has("v") && has("v_size")) {
						v1 = get("v").asInt
						v2 = get("v_size").asInt + v1
					} else if (has("v") && has("height")) {
						v1 = get("u").asInt
						v2 = get("height").asInt + v1
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

}