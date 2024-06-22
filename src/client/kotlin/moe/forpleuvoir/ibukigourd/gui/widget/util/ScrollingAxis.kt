package moe.forpleuvoir.ibukigourd.gui.widget.util

import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@JvmInline
value class ScrollingAxis(val value: Boolean) : Serializable {

    companion object : Deserializer<ScrollingAxis> {

        @JvmStatic
        val X = ScrollingAxis(true)

        @JvmStatic
        val Y = ScrollingAxis(false)

        override fun deserialization(serializeElement: SerializeElement): ScrollingAxis {
            return when (serializeElement.asString) {
                "y", "Y", "false" -> Y
                "x", "X", "true"  -> X
                else              -> throw IllegalArgumentException("Unknown NextAction: $serializeElement")
            }
        }

    }

    @OptIn(ExperimentalContracts::class)
    inline fun isX(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (value) block()
    }

    @OptIn(ExperimentalContracts::class)
    inline fun isY(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (!value) block()
    }

    override fun serialization(): SerializeElement {
        return SerializePrimitive(if (value) "x" else "y")
    }

}