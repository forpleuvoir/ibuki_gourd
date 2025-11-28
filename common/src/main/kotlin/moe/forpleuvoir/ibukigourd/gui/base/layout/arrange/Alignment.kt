@file:Suppress("DuplicatedCode", "unused")

package moe.forpleuvoir.ibukigourd.gui.base.layout.arrange

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import org.joml.Vector2fc

interface Alignment {

    /**
     * 计算对齐之后的位置信息
     * @param space 外部空间大小
     * @param size 需要被对齐的大小
     * @return [Vector2fc]对齐之后的位置偏移
     */
    fun align(space: Size<Float>, size: Size<Float>): Vector2fc

    //1D
    fun interface Linear {
        fun align(space: Float, size: Float): Float
    }

    fun interface Horizontal : Linear {

        operator fun plus(other: Vertical): Alignment = CombinedAlignment(this, other)

    }

    fun interface Vertical : Linear {

        operator fun plus(other: Horizontal): Alignment = CombinedAlignment(other, this)

    }

    companion object {
        //------------ 2D Alignment ------------\\

        val TopLeft: Alignment = BiasAlignment(-1f, -1f)
        val TopCenter: Alignment = BiasAlignment(0f, -1f)
        val TopRight: Alignment = BiasAlignment(1f, -1f)
        val CenterLeft: Alignment = BiasAlignment(-1f, 0f)
        val Center: Alignment = BiasAlignment(0f, 0f)
        val CenterRight: Alignment = BiasAlignment(1f, 0f)
        val BottomLeft: Alignment = BiasAlignment(-1f, 1f)
        val BottomCenter: Alignment = BiasAlignment(0f, 1f)
        val BottomRight: Alignment = BiasAlignment(1f, 1f)

        fun biasedBy(horizontalBias: Float, verticalBias: Float): Alignment = BiasAlignment(horizontalBias, verticalBias)


        //------------ 1D Alignment ------------\\

        val Top: Vertical = BiasAlignment.Vertical(-1f)
        val CenterVertically: Vertical = BiasAlignment.Vertical(0f)
        val Bottom: Vertical = BiasAlignment.Vertical(1f)

        val Left: Horizontal = BiasAlignment.Horizontal(-1f)
        val CenterHorizontally: Horizontal = BiasAlignment.Horizontal(0f)
        val Right: Horizontal = BiasAlignment.Horizontal(1f)
    }

}


private class CombinedAlignment(
    private val horizontal: Alignment.Horizontal,
    private val vertical: Alignment.Vertical,
) : Alignment {
    override fun align(space: Size<Float>, size: Size<Float>): Vector2fc {
        val x = horizontal.align(size.width, space.width)
        val y = vertical.align(size.height, space.height)
        return Vector2f(x, y)
    }
}

data class BiasAlignment(val horizontalBias: Float, val verticalBias: Float) : Alignment {

    override fun align(space: Size<Float>, size: Size<Float>): Vector2fc {
        val centerX = (space.width - size.width) / 2f
        val centerY = (space.height - size.height) / 2f
        val x = centerX * (1 + horizontalBias)
        val y = centerY * (1 + verticalBias)
        return Vector2f(x, y)
    }

    sealed interface Linear : Alignment.Linear {

        val bias: Float

        override fun align(space: Float, size: Float): Float {
            val center = (space - size) / 2f
            return center * (1 + bias)
        }
    }

    data class Horizontal(override val bias: Float) : Linear, Alignment.Horizontal {
        override fun plus(other: Alignment.Vertical): Alignment {
            return when (other) {
                is Vertical -> BiasAlignment(bias, other.bias)
                else        -> super.plus(other)
            }
        }
    }

    data class Vertical(override val bias: Float) : Linear, Alignment.Vertical {
        override fun plus(other: Alignment.Horizontal): Alignment {
            return when (other) {
                is Horizontal -> BiasAlignment(other.bias, bias)
                else          -> super.plus(other)
            }
        }
    }


}