package moe.forpleuvoir.ibukigourd.gui.base.element

data class ElementMeasureDimension(val width: MeasureDimension, val height: MeasureDimension)

@Suppress("NOTHING_TO_INLINE")
inline infix fun MeasureDimension.with(height: MeasureDimension) = ElementMeasureDimension(this, height)

data class MeasureDimension(
    val mode: Mode,
    val value: Float
) {
    enum class Mode {
        UNSPECIFIED,
        EXACTLY,
        AT_MOST
    }
}