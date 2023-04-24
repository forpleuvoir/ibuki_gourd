package com.forpleuvoir.ibukigourd.render.base.texture

import com.forpleuvoir.ibukigourd.render.base.math.Vector4
import com.forpleuvoir.nebula.serialization.base.SerializeElement
import com.forpleuvoir.nebula.serialization.base.SerializeObject
import com.forpleuvoir.nebula.serialization.extensions.getOr

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

	constructor(vector4: Vector4<Number>) : this(
		vector4.x.toInt(), vector4.y.toInt(), vector4.z.toInt(), vector4.w.toInt()
	)

	constructor(corner: Int) : this(corner, corner, corner, corner)

	companion object {

		fun deserialization(serializeElement: SerializeElement?, default: Corner): Corner {
			if (serializeElement == null || serializeElement.isNull) return default
			return try {
				if (serializeElement is SerializeObject)
					serializeElement.let {
						val left: Int
						val right: Int
						if (it.containsKey("vertical")) {
							left = it["vertical"]!!.asInt
							right = it["vertical"]!!.asInt
						} else {
							left = it.getOr("left", default.left).toInt()
							right = it.getOr("right", default.right).toInt()
						}
						val top: Int
						val bottom: Int
						if (it.containsKey("horizontal")) {
							top = it["horizontal"]!!.asInt
							bottom = it["horizontal"]!!.asInt
						} else {
							top = it.getOr("top", default.top).toInt()
							bottom = it.getOr("bottom", default.bottom).toInt()
						}
						Corner(left, right, top, bottom)
					}
				else if (serializeElement.isPrimitive)
					Corner(serializeElement.asInt)
				else default
			} catch (_: Exception) {
				default
			}
		}

	}


	override fun toString(): String {
		return "Corner(left=$left, right=$right, top=$top, bottom=$bottom)"
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Corner

		if (left != other.left) return false
		if (right != other.right) return false
		if (top != other.top) return false
		return bottom == other.bottom
	}

	override fun hashCode(): Int {
		var result = left
		result = 31 * result + right
		result = 31 * result + top
		result = 31 * result + bottom
		return result
	}


}