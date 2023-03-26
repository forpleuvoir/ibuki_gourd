package com.forpleuvoir.ibukigourd.render.base.texture

import com.google.gson.JsonObject

open class TextureUVMapping(
	val corner: Corner,
	u1: Int, v1: Int, u2: Int, v2: Int
) : UVMapping(u1, v1, u2, v2) {
	constructor(corner: Corner, uvMapping: UVMapping) : this(
		corner,
		uvMapping.u1, uvMapping.v1, uvMapping.u2, uvMapping.v2
	)

	companion object {

		fun fromJson(jsonObject: JsonObject?, default: TextureUVMapping): TextureUVMapping {
			if (jsonObject == null) return default
			jsonObject.apply {
				val corner = Corner.fromJson(get("corner"), default.corner)
				val uv = UVMapping.fromJson(jsonObject, default)
				return TextureUVMapping(corner, uv)
			}
		}

	}

}