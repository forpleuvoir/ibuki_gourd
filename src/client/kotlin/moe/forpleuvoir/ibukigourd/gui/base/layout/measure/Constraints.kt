package moe.forpleuvoir.ibukigourd.gui.base.layout.measure

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import kotlin.math.max
import kotlin.math.min

data class Constraints(
    val minWidth: Float = 0f,
    val maxWidth: Float = 114514f,
    val minHeight: Float = 0f,
    val maxHeight: Float = 114514f,
) {

    companion object {
        fun of(
            minWidth: Float = 0f,
            maxWidth: Float = 114514f,
            minHeight: Float = 0f,
            maxHeight: Float = 114514f,
        ) = Constraints(
            minWidth.coerceAtLeast(0f),
            maxWidth.coerceAtLeast(0f),
            minHeight.coerceAtLeast(0f),
            maxHeight.coerceAtLeast(0f)
        )

        fun of(
            minSize: Size<Float> = Size(0f, 0f),
            maxSize: Size<Float> = Size(114514f, 114514f),
        ) = of(minSize.width, maxSize.width, minSize.height, maxSize.height)

    }

    val widthRange get() = minWidth..maxWidth

    val heightRange get() = minHeight..maxHeight

    fun constraintAs(constraints: Constraints): Constraints {
        val minWidth = max(this.minWidth, constraints.minWidth).coerceIn(constraints.minWidth, constraints.maxWidth)
        val maxWidth = min(this.maxWidth, constraints.maxWidth).coerceIn(minWidth, constraints.maxWidth)
        val minHeight = max(this.minHeight, constraints.minHeight).coerceIn(constraints.minHeight, constraints.maxHeight)
        val maxHeight = min(this.maxHeight, constraints.maxHeight).coerceIn(minHeight, constraints.maxHeight)
        return Constraints(minWidth, maxWidth, minHeight, maxHeight)
    }

    fun fixed(): Boolean =
        minWidth == maxWidth && minHeight == maxHeight

    fun widthFixed(): Boolean =
        minWidth == maxWidth

    fun heightFixed(): Boolean =
        minHeight == maxHeight
}
