package moe.forpleuvoir.ibukigourd.util

import kotlin.reflect.KProperty


fun <T> delegate(value: T) = DelegatedValue(value)

class DelegatedValue<T>(private var value: T) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

    fun setValue(value: T) {
        this.value = value
    }

    fun getValue(): T {
        return value
    }

}