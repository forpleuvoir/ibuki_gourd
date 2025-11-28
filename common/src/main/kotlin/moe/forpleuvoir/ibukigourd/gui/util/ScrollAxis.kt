package moe.forpleuvoir.ibukigourd.gui.util

import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@JvmInline
value class ScrollAxis private constructor(val value: Byte) : Serializable {

    companion object : Deserializer<ScrollAxis> {

        @JvmStatic
        val None = ScrollAxis(-1)

        @JvmStatic
        val All = ScrollAxis(0)

        @JvmStatic
        val X = ScrollAxis(1)

        @JvmStatic
        val Y = ScrollAxis(2)

        override fun deserialization(serializeElement: SerializeElement): ScrollAxis {
            return when (serializeElement.asString) {
                "none", "NONE", "None", "-1" -> None
                "all", "ALL", "All", "0"     -> All
                "x", "X", "1"                -> X
                "y", "Y", "2"                -> Y
                else                         -> throw IllegalArgumentException("Unknown ScrollingAxis: $serializeElement")
            }
        }

    }


    val isX: Boolean get() = value == X.value || value == All.value

    @OptIn(ExperimentalContracts::class)
    inline fun isX(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (isX) block()
    }

    val isY: Boolean get() = value == Y.value || value == All.value

    @OptIn(ExperimentalContracts::class)
    inline fun isY(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (isY) block()
    }

    val isNone: Boolean get() = value == None.value

    @OptIn(ExperimentalContracts::class)
    inline fun isNone(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (isNone) block()
    }

    val isAll: Boolean get() = value == All.value

    @OptIn(ExperimentalContracts::class)
    inline fun isAll(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (isAll) block()
    }

    override fun serialization(): SerializeElement {
        return SerializePrimitive(
            when (value) {
                (-1).toByte() -> "none"
                0.toByte()    -> "all"
                1.toByte()    -> "X"
                2.toByte()    -> "Y"
                else          -> "none"
            }
        )
    }

}