package com.forpleuvoir.ibukigourd.gui.texture

import com.forpleuvoir.ibukigourd.render.base.texture.Corner
import com.forpleuvoir.ibukigourd.render.base.texture.TextureInfo
import com.forpleuvoir.ibukigourd.render.base.texture.TextureUVMapping
import com.google.gson.JsonObject

class WidgetTexture(
	corner: Corner,
	u1: Int, v1: Int, u2: Int, v2: Int,
	val textureInfo: TextureInfo
) : TextureUVMapping(corner, u1, v1, u2, v2) {

	constructor(textureUVMapping: TextureUVMapping, textureInfo: TextureInfo) : this(
		textureUVMapping.corner,
		textureUVMapping.u1,
		textureUVMapping.v1,
		textureUVMapping.u2,
		textureUVMapping.v2,
		textureInfo
	)

	companion object {
		fun fromJson(jsonObject: JsonObject?, default: WidgetTexture): WidgetTexture {
			if (jsonObject == null) return default
			jsonObject.apply {
				val textureUVMapping = TextureUVMapping.fromJson(this, default)
				val textureInfo = TextureInfo.fromJson(get("texture_info"), default.textureInfo)
				return WidgetTexture(textureUVMapping, textureInfo)
			}
		}
	}

}