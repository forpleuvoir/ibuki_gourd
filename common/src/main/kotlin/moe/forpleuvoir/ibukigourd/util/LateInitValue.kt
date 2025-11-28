package moe.forpleuvoir.ibukigourd.util

import kotlin.reflect.KProperty

data class LateInitValue<T>(private var _value: T? = null) {

    fun getValue(): T = _value ?: throw IllegalStateException("value has not been initialized")

    fun setValue(value: T) {
        if (_value == null) this._value = value
        else throw IllegalStateException("value has been initialized")
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>?): T = getValue()

    operator fun setValue(thisRef: Any?, property: KProperty<*>?, value: T) = setValue(value)

    val isInit: Boolean
        get() = _value != null

}

fun <T> lateInitValueOf(value: T? = null) = LateInitValue(value)