package moe.forpleuvoir.ibukigourd.gui.base.element

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