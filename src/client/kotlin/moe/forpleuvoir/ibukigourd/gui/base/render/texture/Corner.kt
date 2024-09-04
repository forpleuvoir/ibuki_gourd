package moe.forpleuvoir.ibukigourd.gui.base.render.texture

import moe.forpleuvoir.ibukigourd.gui.base.render.SizeInt
import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject

data class Corner(
    val left: Int = 0,
    val right: Int = 0,
    val top: Int = 0,
    val bottom: Int = 0
) : SizeInt {
    constructor(vertical: Int = 0, horizontal: Int = 0) : this(
        left = horizontal,
        right = horizontal,
        top = vertical,
        bottom = vertical
    )

    constructor(corner: Int = 0) : this(corner, corner, corner, corner)

    override val width: Int get() = right + left

    override val height: Int get() = bottom + top


    companion object : Serializer<Corner>, Deserializer<Corner> {

        val Unspecified = Corner(0)

        override fun deserialization(serializeElement: SerializeElement): Corner {
            return serializeElement.checkType<Corner>()
                .check<SerializeObject> {
                    val left: Int
                    val right: Int
                    if (it.containsKey("vertical")) {
                        left = it["vertical"]!!.asInt
                        right = left
                    } else {
                        left = it["left"]!!.asInt
                        right = it["right"]!!.asInt
                    }
                    val top: Int
                    val bottom: Int
                    if (it.containsKey("horizontal")) {
                        top = it["horizontal"]!!.asInt
                        bottom = top
                    } else {
                        top = it["top"]!!.asInt
                        bottom = it["bottom"]!!.asInt
                    }
                    Corner(left, right, top, bottom)
                }
                .check<SerializePrimitive> {
                    Corner(it.asInt)
                }.getOrThrow()
        }

        override fun serialization(target: Corner): SerializeElement {
            return serializeObject {
                "left" to target.left
                "right" to target.right
                "top" to target.top
                "bottom" to target.bottom
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