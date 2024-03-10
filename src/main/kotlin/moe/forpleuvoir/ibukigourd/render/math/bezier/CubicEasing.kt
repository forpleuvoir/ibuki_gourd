package moe.forpleuvoir.ibukigourd.render.math.bezier

import kotlin.math.pow

object CubicEasing : Easing {
    override fun easeIn(t: Float): Float = t * t * t

    override fun easeOut(t: Float): Float = 1f - (1f - t).pow(3f)

    override fun easeInOut(t: Float): Float = if (t < 0.5f) 4f * t * t * t else 1f - (-2f * t + 2f).pow(3f) / 2f

}