package moe.forpleuvoir.ibukigourd.util

import moe.forpleuvoir.nebula.common.api.Notifiable
import java.util.function.Consumer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KProperty

fun <T> stateOf(value: T) = State(value)

data class State<T>(private var value: T) : Notifiable<T> {

    var onSetValue: (T) -> T = { it }

    var onGetValue: (T) -> T = { it }

    var enableNotification: Boolean = true

    @OptIn(ExperimentalContracts::class)
    inline fun disableNotification(action: () -> Unit) {
        contract {
            callsInPlace(action, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
        }
        enableNotification = false
        action()
        enableNotification = true
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return onGetValue(value)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val oldValue = this.value
        this.value = onSetValue(value)
        if (oldValue != value) {
            onChange(this.value)
        }
    }

    fun setValue(value: T) {
        val oldValue = this.value
        this.value = onSetValue(value)
        if (oldValue != value) {
            onChange(this.value)
        }
    }

    fun getValue(): T {
        return onGetValue(value)
    }

    private val observers: MutableList<Consumer<T>> = ArrayList()

    override fun onChange(value: T) {
        if (enableNotification) observers.forEach { it.accept(value) }
    }

    override fun subscribe(callback: Consumer<T>) {
        observers.add(callback)
    }


    fun <A> bind(otherState: State<A>, map: (A) -> T) {
        otherState.subscribe {
            this.setValue(map(it))
        }
    }

    override fun toString(): String {
        return value.toString()
    }

    companion object {

        fun <A, B> bind(a: State<A>, b: State<B>, mapA2B: (A) -> B, mapB2A: (B) -> A) {
            a.subscribe {
                a.disableNotification {
                    b.setValue(mapA2B(it))
                }
            }
            b.subscribe {
                b.disableNotification {
                    a.setValue(mapB2A(it))
                }
            }
        }

    }

}

operator fun State<String>.plus(other: Any?): State<String> {
    this.setValue(this.getValue() + other.toString())
    return this
}

fun State<String>.append(other: Any?): State<String> {
    this.setValue(this.getValue() + other.toString())
    return this
}

operator fun State<String>.plusAssign(other: State<String>) {
    this.setValue(this.getValue() + other.toString())
}

operator fun State<Boolean>.not(): State<Boolean> {
    this.setValue(!this.getValue())
    return this
}

fun State<Boolean>.switch(): State<Boolean> {
    this.setValue(!this.getValue())
    return this
}
