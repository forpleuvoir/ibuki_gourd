package moe.forpleuvoir.ibukigourd.gui.base.measure

data class Constraints(
    val minWidth: Float = 0f,
    val maxWidth: Float = Float.POSITIVE_INFINITY,
    val minHeight: Float = 0f,
    val maxHeight: Float = Float.POSITIVE_INFINITY
)
