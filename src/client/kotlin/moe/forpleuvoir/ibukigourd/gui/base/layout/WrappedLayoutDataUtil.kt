package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable

fun interface WrappedLayoutDataUtil<T : Any> {

    fun default(): T

    @Suppress("UNCHECKED_CAST")
    fun fromMeasurable(measurable: Measurable) =
        measurable.parentData as? T


    fun getOrDefault(measurable: Measurable, default: T = this.default()) =
        fromMeasurable(measurable) ?: default

    fun wrappedDatas(list: List<Measurable>, default: T = this.default()): List<T> {
        return list.map { getOrDefault(it, default) }
    }

}