package com.forpleuvoir.ibukigourd.render.base.texture

import com.forpleuvoir.nebula.serialization.base.SerializeElement

open class TextureUVMapping(
	val corner: Corner,
	u1: Int, v1: Int, u2: Int, v2: Int
) : UVMapping(u1, v1, u2, v2) {
	constructor(corner: Corner, uvMapping: UVMapping) : this(
		corner,
		uvMapping.u1, uvMapping.v1, uvMapping.u2, uvMapping.v2
	)

	companion object {

		fun deserialization(serializeElement: SerializeElement?, default: TextureUVMapping): TextureUVMapping {
			if (serializeElement == null || !serializeElement.isObject) return default
			serializeElement.asObject.apply {
				val corner = Corner.deserialization(get("corner"), default.corner)
				val uv = UVMapping.deserialization(this, default)
				return TextureUVMapping(corner, uv)
			}
		}

	}


	override fun toString(): String {
		return "TextureUVMapping(corner=$corner,u1=$u1, v1=$v1, u2=$u2, v2=$v2)"
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		if (!super.equals(other)) return false

		other as TextureUVMapping

		return corner == other.corner
	}

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + corner.hashCode()
		return result
	}

}