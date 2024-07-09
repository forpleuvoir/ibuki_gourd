package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializer
import moe.forpleuvoir.nebula.serialization.base.SerializeArray
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject

data class Padding(
    val left: Int,
    val right: Int,
    val top: Int,
    val bottom: Int
) {

    constructor(left: Number, right: Number, top: Number, bottom: Number) : this(
        left.toInt(), right.toInt(), top.toInt(), bottom.toInt()
    )

    constructor(horizontal: Number = 0, vertical: Number = 0) : this(horizontal, horizontal, vertical, vertical)

    constructor(padding: Number) : this(padding, padding, padding, padding)

    val width get() = left + right

    val height get() = top + bottom

    companion object : Deserializer<Padding>, Serializer<Padding> {
        override fun deserialization(serializeElement: SerializeElement): Padding {
            return serializeElement
                .checkType<Padding>()
                .check<SerializePrimitive> {
                    Padding(it.asInt)
                }
                .check<SerializeObject> {
                    when (it.keys) {
                        setOf("left", "right", "top", "bottom") ->
                            Padding(
                                it["left"]!!.asInt,
                                it["right"]!!.asInt,
                                it["top"]!!.asInt,
                                it["bottom"]!!.asInt,
                            )

                        setOf("horizontal", "vertical")         ->
                            Padding(
                                it["horizontal"]!!.asNumber,
                                it["vertical"]!!.asNumber,
                            )

                        else                                    -> throw IllegalArgumentException("The key of the object is wrong, expected [left,right,top,bottom] or [horizontal,vertical].")
                    }
                }
                .check<SerializeArray> {
                    when (it.size) {
                        2    -> {
                            Padding(it[0].asNumber, it[1].asNumber)
                        }

                        4    -> {
                            Padding(it[0].asInt, it[1].asInt, it[2].asInt, it[3].asInt)
                        }

                        else -> throw IllegalArgumentException("The size of the array is wrong, expected [2] or [4].")
                    }
                }.getOrThrow()
        }

        override fun serialization(target: Padding): SerializeElement = serializeObject {
            "left" - target.left
            "right" - target.right
            "top" - target.top
            "bottom" - target.bottom
        }

    }

}