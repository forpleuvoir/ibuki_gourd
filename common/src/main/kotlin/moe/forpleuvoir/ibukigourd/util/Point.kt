package moe.forpleuvoir.ibukigourd.util

import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject

data class Point(val x: Int, val y: Int) : Serializable {

    companion object : Deserializer<Point> {
        override fun deserialization(serializeElement: SerializeElement): Point {
            return serializeElement.checkType {
                check<SerializeObject> {
                    Point(it["x"]!!.asInt, it["y"]!!.asInt)
                }
            }.getOrThrow()
        }
    }

    override fun serialization(): SerializeElement {
        return serializeObject {
            "x" to x
            "y" to y
        }
    }

}
