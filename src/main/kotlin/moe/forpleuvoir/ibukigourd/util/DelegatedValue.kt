package moe.forpleuvoir.ibukigourd.util

import kotlin.reflect.KProperty


fun <T> delegate(value: T) = DelegatedValue(value)

data class DelegatedValue<T>(private var value: T) {

    var onSetValue: (T) -> T = { it }

    var onGetValue: (T) -> T = { it }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return onGetValue(value)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = onSetValue(value)
    }

    fun setValue(value: T) {
        this.value = onSetValue(value)
    }

    fun getValue(): T {
        return onGetValue(value)
    }

}