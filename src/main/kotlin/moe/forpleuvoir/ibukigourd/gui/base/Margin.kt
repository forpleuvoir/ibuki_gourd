package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializer
import moe.forpleuvoir.nebula.serialization.base.SerializeArray
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.extensions.checkType
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
            return serializeElement
                    .checkType<Margin>()
                    .check<SerializePrimitive> {
                        Margin(it.asFloat)
                    }
                    .check<SerializeObject> {
                        when (it.keys) {
                            setOf("left", "right", "top", "bottom") ->
                                Margin(
                                    it["left"]!!.asFloat,
                                    it["right"]!!.asFloat,
                                    it["top"]!!.asFloat,
                                    it["bottom"]!!.asFloat,
                                )

                            setOf("horizontal", "vertical")         ->
                                Margin(
                                    it["horizontal"]!!.asNumber,
                                    it["vertical"]!!.asNumber,
                                )

                            else                                    -> throw IllegalArgumentException("The key of the object is wrong, expected [left,right,top,bottom] or [horizontal,vertical].")
                        }
                    }
                    .check<SerializeArray> {
                        when (it.size) {
                            2    -> {
                                Margin(it[0].asNumber, it[1].asNumber)
                            }

                            4    -> {
                                Margin(it[0].asFloat, it[1].asFloat, it[2].asFloat, it[3].asFloat)
                            }

                            else -> throw IllegalArgumentException("The size of the array is wrong, expected [2] or [4].")
                        }
                    }.getOrThrow()
        }

        override fun serialization(target: Margin): SerializeElement = serializeObject {
            "left" - target.left
            "right" - target.right
            "top" - target.top
            "bottom" - target.bottom
        }

    }

}