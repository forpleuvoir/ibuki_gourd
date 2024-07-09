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
    val left: Int = 0,
    val right: Int = 0,
    val top: Int = 0,
    val bottom: Int = 0
) {

    constructor(left: Number, right: Number, top: Number, bottom: Number) : this(
        left.toInt(), right.toInt(), top.toInt(), bottom.toInt()
    )

    constructor(horizontal: Number = 0, vertical: Number = 0) : this(horizontal, horizontal, vertical, vertical)

    constructor(margin: Number) : this(margin, margin, margin, margin)

    val width get() = left + right

    val height get() = top + bottom

    companion object : Deserializer<Margin>, Serializer<Margin> {
        override fun deserialization(serializeElement: SerializeElement): Margin {
            return serializeElement
                .checkType<Margin>()
                .check<SerializePrimitive> {
                    Margin(it.asInt)
                }
                .check<SerializeObject> {
                    when (it.keys) {
                        setOf("left", "right", "top", "bottom") ->
                            Margin(
                                it["left"]!!.asInt,
                                it["right"]!!.asInt,
                                it["top"]!!.asInt,
                                it["bottom"]!!.asInt,
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
                            Margin(it[0].asInt, it[1].asInt, it[2].asInt, it[3].asInt)
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