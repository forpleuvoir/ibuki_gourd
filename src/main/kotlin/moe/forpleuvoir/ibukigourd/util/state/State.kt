package moe.forpleuvoir.ibukigourd.util.state

import moe.forpleuvoir.nebula.common.api.Notifiable
import moe.forpleuvoir.nebula.common.util.primitive.pick
import java.util.function.Consumer
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

fun interface State<T> : Notifiable<T> {

    fun getValue(): T

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = getValue()
    override fun onChange(value: T) = Unit
    override fun subscribe(callback: Consumer<T>) = Unit

}

@JvmInline
value class ImmutableState<T>(private val value: T) : State<T> {
    override fun getValue(): T = value
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}


fun <T> stateOf(value: T): State<T> = ImmutableState(value)

fun <T> stateBy(value: () -> T) = State { value() }

fun <T> stateOf(value: KMutableProperty0<T>) = State { value.get() }

fun <T> stateOf(state: State<T>): State<T> = mutableStateOf(state) { it }

fun <A, B> stateOf(state: State<B>, map: (B) -> A): State<A> = State { map(state.getValue()) }

fun <A, B> stateOf(state: MutableState<B>, map: (B) -> A): State<A> = mutableStateOf(state, map)

//------------ Extension ------------\\

operator fun State<Boolean>.not(): Boolean = !this.getValue()

fun <T> State<Boolean>.pick(a: T, b: T) = this.getValue().pick(a, b)

inline fun <R> State<Boolean>.pick(a: () -> R, b: () -> R): R = this.getValue().pick(a, b)