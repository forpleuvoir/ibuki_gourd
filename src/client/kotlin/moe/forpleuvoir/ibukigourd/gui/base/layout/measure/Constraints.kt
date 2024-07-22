package moe.forpleuvoir.ibukigourd.gui.base.layout.measure

import kotlin.math.max
import kotlin.math.min

data class Constraints constructor(
    val minWidth: Float = 0f,
    val maxWidth: Float = Float.POSITIVE_INFINITY,
    val minHeight: Float = 0f,
    val maxHeight: Float = Float.POSITIVE_INFINITY
) {

    companion object {
        fun of(
            minWidth: Float = 0f,
            maxWidth: Float = Float.POSITIVE_INFINITY,
            minHeight: Float = 0f,
            maxHeight: Float = maxWidth
        ) = Constraints(
            minWidth.coerceAtLeast(0f),
            maxWidth.coerceAtLeast(0f),
            minHeight.coerceAtLeast(0f),
            maxHeight.coerceAtLeast(0f)
        )

    }


    fun constraint(constraints: Constraints): Constraints {
        val minWidth = max(this.minWidth, constraints.minWidth).coerceIn(constraints.minWidth, constraints.maxWidth)
        val maxWidth = min(this.maxWidth, constraints.maxWidth).coerceIn(minWidth, constraints.maxWidth)
        val minHeight = max(this.minHeight, constraints.minHeight).coerceIn(constraints.minHeight, constraints.maxHeight)
        val maxHeight = min(this.maxHeight, constraints.maxHeight).coerceIn(minHeight, constraints.maxHeight)
        return Constraints(minWidth, maxWidth, minHeight, maxHeight)
    }

}
