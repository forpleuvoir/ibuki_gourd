package moe.forpleuvoir.ibukigourd.util

import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@JvmInline
value class NextAction private constructor(val value: Boolean) : Serializable {
    companion object : Deserializer<NextAction> {
        @JvmStatic
        val Continue = NextAction(false)

        @JvmStatic
        val Cancel = NextAction(true)
        override fun deserialization(serializeElement: SerializeElement): NextAction {
            return when (serializeElement.asString) {
                "continue" -> Continue
                "cancel"   -> Cancel
                else       -> throw IllegalArgumentException("Unknown NextAction: $serializeElement")
            }
        }

    }

    @OptIn(ExperimentalContracts::class)
    inline fun ifCancel(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (value) block()
    }

    @OptIn(ExperimentalContracts::class)
    inline fun ifContinue(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        if (!value) block()
    }

    override fun serialization(): SerializeElement {
        return SerializePrimitive(if (value) "cancel" else "continue")
    }

}