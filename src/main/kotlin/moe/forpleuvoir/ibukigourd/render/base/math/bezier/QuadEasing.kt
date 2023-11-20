package moe.forpleuvoir.ibukigourd.render.base.math.bezier

import kotlin.math.pow

object QuadEasing : Easing {
    override fun easeIn(t: Float): Float = t * t

    override fun easeOut(t: Float): Float = 1f - (1f - t) * (1f - t)

    override fun easeInOut(t: Float): Float = if (t < 0.5f) 2f * t * t else 1f - (-2f * t + 2f).pow(2f) / 2f

}