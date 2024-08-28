package moe.forpleuvoir.ibukigourd.util

import moe.forpleuvoir.nebula.common.api.Notifiable
import java.util.function.Consumer
import kotlin.reflect.KProperty

fun <T> delegateBy(value: T) = DelegatedValue(value)

data class DelegatedValue<T>(private var value: T) : Notifiable<T> {

    var onSetValue: (T) -> T = { it }

    var onGetValue: (T) -> T = { it }

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
        observers.forEach { it.accept(value) }
    }

    override fun subscribe(callback: Consumer<T>) {
        observers.add(callback)
    }

}