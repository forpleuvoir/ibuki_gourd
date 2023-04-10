package com.forpleuvoir.ibukigourd.render.base.texture

import com.forpleuvoir.ibukigourd.util.resources
import com.forpleuvoir.nebula.serialization.base.SerializeElement
import com.forpleuvoir.nebula.serialization.extensions.getOr
import net.minecraft.util.Identifier

data class TextureInfo(
	val width: Int = 256,
	val height: Int = 256,
	val texture: Identifier
) {
	companion object {
		fun deserialization(serializeElement: SerializeElement?, default: TextureInfo): TextureInfo {
			if (serializeElement == null || !serializeElement.isObject) return default
			serializeElement.asObject.apply {
				return TextureInfo(
					getOr("width", default.width).toInt(),
					getOr("height", default.height).toInt(),
					try {
						resources(this["texture"]!!.asString)
					} catch (_: Exception) {
						default.texture
					}
				)
			}
		}
	}


	override fun toString(): String {
		return "TextureInfo(width=$width, height=$height, texture=$texture)"
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as TextureInfo

		if (width != other.width) return false
		if (height != other.height) return false
		return texture == other.texture
	}

	override fun hashCode(): Int {
		var result = width
		result = 31 * result + height
		result = 31 * result + texture.hashCode()
		return result
	}


}