package moe.forpleuvoir.ibukigourd.render.extension.texture

import moe.forpleuvoir.nebula.common.util.expectedType
import moe.forpleuvoir.nebula.common.util.letNotNull
import moe.forpleuvoir.nebula.common.util.requireTypeOrNull
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.base.builder.build
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.extensions.requireInt

data class Corner(
    val left: Int = 0,
    val right: Int = 0,
    val top: Int = 0,
    val bottom: Int = 0
) {
    constructor(vertical: Int = 0, horizontal: Int = 0) : this(
        left = horizontal,
        right = horizontal,
        top = vertical,
        bottom = vertical
    )

    constructor(corner: Int = 0) : this(corner, corner, corner, corner)

    val width: Int = right + left

    val height: Int = bottom + top

    val isSpecified = this != Unspecified

    companion object : Codec<Corner> {

        val Unspecified = Corner(0)

        override fun deserialization(data: SerializeElement): Result<Corner> = DeserializationException.runCatching<Corner> {
            data.requireTypeOrNull<SerializeObject>().letNotNull {
                val left: Int
                val right: Int
                if (it.containsKey("vertical")) {
                    left = it.requireInt("vertical")
                    right = left
                } else {
                    left = it.requireInt("left")
                    right = it.requireInt("right")
                }
                val top: Int
                val bottom: Int
                if (it.containsKey("horizontal")) {
                    top = it.requireInt("horizontal")
                    bottom = top
                } else {
                    top = it.requireInt("top")
                    bottom = it.requireInt("bottom")
                }
                Corner(left, right, top, bottom)
            } ?: data.requireTypeOrNull<SerializePrimitive>().letNotNull {
                Corner(it.asInt ?: throw expectedType(it.valueType, Int::class))
            } ?: throw expectedType(data::class, SerializeObject::class, SerializePrimitive::class)
        }

        override fun serialization(target: Corner): SerializeElement = SerializeObject.build {
            "left" to target.left
            "right" to target.right
            "top" to target.top
            "bottom" to target.bottom
        }

    }

}