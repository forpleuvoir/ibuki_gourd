package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject

typealias Padding = Margin

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
            if (serializeElement.isPrimitive) {
                return Margin(serializeElement.asNumber)
            } else if (serializeElement.isObject)
                serializeElement.asObject.apply {
                    if (this.keys == setOf("left", "right", "top", "bottom"))
                        return Margin(
                            this["left"]!!.asFloat,
                            this["right"]!!.asFloat,
                            this["top"]!!.asFloat,
                            this["bottom"]!!.asFloat,
                        )
                    else if (this.keys == setOf("horizontal", "vertical")) {
                        return Margin(
                            this["horizontal"]!!.asNumber,
                            this["vertical"]!!.asNumber,
                        )
                    }
                }
            else if (serializeElement.isArray) {
                serializeElement.asArray.apply {
                    if (this.size == 2) {
                        return Margin(this[0].asNumber, this[1].asNumber)
                    } else if (this.size == 4) {
                        return Margin(this[0].asFloat, this[1].asFloat, this[2].asFloat, this[3].asFloat)
                    }
                }
            }
            throw DeserializationException(
                "serialization format error,expected SerializeObject or SerializePrimitive and SerializeArray but was ${serializeElement::class.simpleName}," +
                "If the type is correct, then the corresponding key is wrong, or the length of the array is wrong(The length of the array is 2 or 4)."
            )
        }

        override fun serialization(target: Margin): SerializeElement = serializeObject {
            "left" - target.left
            "right" - target.right
            "top" - target.top
            "bottom" - target.bottom
        }

    }

}