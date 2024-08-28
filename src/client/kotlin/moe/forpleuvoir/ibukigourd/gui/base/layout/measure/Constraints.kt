package moe.forpleuvoir.ibukigourd.gui.base.layout.measure

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.scaledSize
import kotlin.math.max
import kotlin.math.min

data class Constraints(
    val minWidth: Float = 0f,
    val maxWidth: Float = mc.window.scaledWidth.toFloat(),
    val minHeight: Float = 0f,
    val maxHeight: Float = mc.window.scaledHeight.toFloat(),
) {

    companion object {
        fun of(
            minWidth: Float = 0f,
            maxWidth: Float = mc.window.scaledWidth.toFloat(),
            minHeight: Float = 0f,
            maxHeight: Float = mc.window.scaledHeight.toFloat(),
        ) = Constraints(
            minWidth.coerceAtLeast(0f),
            maxWidth.coerceAtLeast(0f),
            minHeight.coerceAtLeast(0f),
            maxHeight.coerceAtLeast(0f)
        )

        fun of(
            minSize: Size<Float> = Size(0f, 0f),
            maxSize: Size<Float> = mc.window.scaledSize.toFloat(),
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
}
