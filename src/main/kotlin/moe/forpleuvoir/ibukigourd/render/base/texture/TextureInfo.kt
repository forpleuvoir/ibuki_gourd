package moe.forpleuvoir.ibukigourd.render.base.texture

import moe.forpleuvoir.ibukigourd.util.resources
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.extensions.getOr
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
					runCatching { resources(this["texture"]!!.asString) }.getOrDefault(default.texture)
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