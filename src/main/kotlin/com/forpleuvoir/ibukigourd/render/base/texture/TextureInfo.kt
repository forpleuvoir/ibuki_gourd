package com.forpleuvoir.ibukigourd.render.base.texture

import com.forpleuvoir.nebula.serialization.json.getOr
import com.google.gson.JsonElement
import net.minecraft.util.Identifier

data class TextureInfo(
	val width: Int = 256,
	val height: Int = 256,
	val texture: Identifier
) {
	companion object {
		fun fromJson(jsonObject: JsonElement?, default: TextureInfo): TextureInfo {
			if (jsonObject == null || !jsonObject.isJsonObject) return default
			jsonObject.asJsonObject.apply {
				return TextureInfo(
					getOr("width", default.width).toInt(),
					getOr("height", default.height).toInt(),
					getOr("texture", default.texture)
				)
			}
		}
	}

}