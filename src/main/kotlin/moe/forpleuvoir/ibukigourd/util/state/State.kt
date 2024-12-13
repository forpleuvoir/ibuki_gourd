package moe.forpleuvoir.ibukigourd.util.state

import moe.forpleuvoir.nebula.common.api.Notifiable
import moe.forpleuvoir.nebula.common.util.primitive.pick
import java.util.function.Consumer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KProperty

sealed interface State<T> : Notifiable<T> {

    fun getValue(): T

    var enableNotification: Boolean

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = getValue()
    override fun onChange(value: T) = Unit
    override fun subscribe(callback: Consumer<T>) = Unit
}

@OptIn(ExperimentalContracts::class)
inline fun State<*>.disableNotification(action: () -> Unit) {
    contract {
        callsInPlace(action, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    enableNotification = false
    action()
    enableNotification = true
}

@JvmInline
value class ImmutableState<T>(private val value: T) : State<T> {
    override fun getValue(): T = value
    override var enableNotification: Boolean
        get() = true
        set(value) {}

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    override fun toString(): String = value.toString()
}

fun <T> stateOf(value: T): State<T> = ImmutableState(value)

//------------ Extension ------------\\

operator fun State<Boolean>.not(): Boolean = !this.getValue()

fun <T> State<Boolean>.pick(a: T, b: T) = this.getValue().pick(a, b)

inline fun <R> State<Boolean>.pick(a: () -> R, b: () -> R): R = this.getValue().pick(a, b)