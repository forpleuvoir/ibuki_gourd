package moe.forpleuvoir.ibukigourd.gui.base.layout.measure

import kotlin.math.max
import kotlin.math.min

data class Constraints(
    val minWidth: Float = 0f,
    val maxWidth: Float = Float.POSITIVE_INFINITY,
    val minHeight: Float = 0f,
    val maxHeight: Float = Float.POSITIVE_INFINITY
) {

    fun constraint(constraints: Constraints): Constraints {
        val minWidth = max(this.minWidth, constraints.minWidth).coerceAtMost(min(this.maxWidth, constraints.maxWidth))
        val maxWidth = min(this.maxWidth, constraints.maxWidth).coerceAtLeast(minWidth)
        val minHeight = max(this.minHeight, constraints.minHeight).coerceAtMost(min(this.maxHeight, constraints.maxHeight))
        val maxHeight = min(this.maxHeight, constraints.maxHeight).coerceAtLeast(minHeight)
        return Constraints(minWidth, maxWidth, minHeight, maxHeight)
    }

}
