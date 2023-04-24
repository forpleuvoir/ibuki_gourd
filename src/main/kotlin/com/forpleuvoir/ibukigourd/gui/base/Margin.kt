package com.forpleuvoir.ibukigourd.gui.base

import com.forpleuvoir.nebula.serialization.Deserializer
import com.forpleuvoir.nebula.serialization.Serializer
import com.forpleuvoir.nebula.serialization.base.SerializeElement
import com.forpleuvoir.nebula.serialization.extensions.serializeObject

data class Margin(
	val left: Float = 0.0f,
	val right: Float = 0.0f,
	val top: Float = 0.0f,
	val bottom: Float = 0.0f
) {

	constructor(left: Number, right: Number, top: Number, bottom: Number) : this(
		left.toFloat(), right.toFloat(), top.toFloat(), bottom.toFloat()
	)

	constructor(horizontal: Number = 0f, vertical: Number = 0f) : this(horizontal, horizontal, vertical, vertical)

	constructor(margin: Number) : this(margin, margin, margin, margin)

	val width get() = left + right

	val height get() = top + bottom

	companion object : Deserializer<Margin>, Serializer<Margin> {
		override fun deserialization(serializeElement: SerializeElement): Margin {
			serializeElement.asObject.apply {
				return Margin(this["left"]!!.asFloat)
			}
		}

		override fun serialization(target: Margin): SerializeElement = serializeObject {
			"left" - target.left
			"right" - target.right
			"top" - target.top
			"bottom" - target.bottom
		}

	}

}