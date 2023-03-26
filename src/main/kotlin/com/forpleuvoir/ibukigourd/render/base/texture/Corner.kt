package com.forpleuvoir.ibukigourd.render.base.texture

import com.forpleuvoir.nebula.serialization.json.getOr
import com.google.gson.JsonElement

data class Corner(
	val left: Int,
	val right: Int,
	val top: Int,
	val bottom: Int,
) {
	constructor(vertical: Int, horizontal: Int) : this(
		left = vertical,
		right = vertical,
		top = horizontal,
		bottom = horizontal
	)

	constructor(corner: Int) : this(corner, corner, corner, corner)

	companion object {

		fun fromJson(jsonElement: JsonElement?, default: Corner): Corner {
			if (jsonElement == null) return default
			return try {
				if (jsonElement.isJsonObject)
					jsonElement.asJsonObject.let {
						val left: Int
						val right: Int
						if (it.has("vertical")) {
							left = it.get("vertical").asInt
							right = it.get("vertical").asInt
						} else {
							left = it.getOr("left", default.left).toInt()
							right = it.getOr("right", default.right).toInt()
						}
						val top: Int
						val bottom: Int
						if (it.has("horizontal")) {
							top = it.get("horizontal").asInt
							bottom = it.get("horizontal").asInt
						} else {
							top = it.getOr("top", default.top).toInt()
							bottom = it.getOr("bottom", default.bottom).toInt()
						}
						Corner(left, right, top, bottom)
					}
				else if (jsonElement.isJsonPrimitive)
					Corner(jsonElement.asJsonPrimitive.asInt)
				else throw Exception()
			} catch (_: Exception) {
				default
			}
		}

	}

}