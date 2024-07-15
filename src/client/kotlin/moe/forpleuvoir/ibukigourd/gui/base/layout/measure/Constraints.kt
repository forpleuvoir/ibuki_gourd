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
        val minWidth = max(this.minWidth, constraints.minWidth)
        val maxWidth = min(this.maxWidth, constraints.maxWidth)
        val minHeight = max(this.minHeight, constraints.minHeight)
        val maxHeight = min(this.maxHeight, constraints.maxHeight)
        return Constraints(minWidth, maxWidth, minHeight, maxHeight)
    }

}
