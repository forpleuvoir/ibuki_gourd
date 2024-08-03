package moe.forpleuvoir.ibukigourd.gui.util

import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@JvmInline
value class ScrollState private constructor(val value: Byte) : Serializable {

    companion object : Deserializer<ScrollState> {

        @JvmStatic
        val None = ScrollState(0)

        @JvmStatic
        val Forward = ScrollState(1)

        @JvmStatic
        val Back = ScrollState(2)

        override fun deserialization(serializeElement: SerializeElement): ScrollState {
            return when (serializeElement.asString) {
                "none", "NONE", "None", "0"          -> None
                "forward", "FORWARD", "Forward", "1" -> Forward
                "back", "BACK", "Back", "2"          -> Back
                else                                 -> throw IllegalArgumentException("Unknown ScrollState: $serializeElement")
            }
        }

    }


    val isForward: Boolean get() = value == Forward.value

    @OptIn(ExperimentalContracts::class)
    inline fun isForward(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (isForward) block()
    }

    val isBack: Boolean get() = value == Back.value

    @OptIn(ExperimentalContracts::class)
    inline fun isY(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (isBack) block()
    }

    val isNone: Boolean get() = value == None.value

    @OptIn(ExperimentalContracts::class)
    inline fun isNone(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (isNone) block()
    }

    override fun serialization(): SerializeElement {
        return SerializePrimitive(
            when (value) {
                0.toByte() -> "none"
                1.toByte() -> "forward"
                2.toByte() -> "back"
                else       -> "none"
            }
        )
    }

}