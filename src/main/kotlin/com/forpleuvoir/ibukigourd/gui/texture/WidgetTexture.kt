package com.forpleuvoir.ibukigourd.gui.texture

import com.forpleuvoir.ibukigourd.render.base.texture.Corner
import com.forpleuvoir.ibukigourd.render.base.texture.TextureInfo
import com.forpleuvoir.ibukigourd.render.base.texture.TextureUVMapping
import com.forpleuvoir.nebula.serialization.base.SerializeElement

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
		fun deserialization(serializeElement: SerializeElement?, default: WidgetTexture): WidgetTexture {
			if (serializeElement == null || !serializeElement.isObject) return default
			serializeElement.asObject.apply {
				val textureUVMapping = TextureUVMapping.deserialization(this, default)
				val textureInfo = TextureInfo.deserialization(get("texture_info"), default.textureInfo)
				return WidgetTexture(textureUVMapping, textureInfo)
			}
		}

	}


	override fun toString(): String {
		return "WidgetTexture(corner=$corner,u1=$u1, v1=$v1, u2=$u2, v2=$v2,textureInfo=$textureInfo)"
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		if (!super.equals(other)) return false

		other as WidgetTexture

		return textureInfo == other.textureInfo
	}

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + textureInfo.hashCode()
		return result
	}


}