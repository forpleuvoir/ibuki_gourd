package moe.forpleuvoir.ibukigourd.gui.base.layout

import moe.forpleuvoir.ibukigourd.gui.base.layout.measure.Measurable

interface WrappedLayoutDataUtil<T> {

    fun default(): T

    fun fromMeasurable(measurable: Measurable): T?

    fun getOrDefault(measurable: Measurable, default: T = this.default()) =
        fromMeasurable(measurable) ?: default

    fun wrappedData(list: List<Measurable>, default: T = this.default()): List<T> {
        return list.map { getOrDefault(it, default) }
    }

}