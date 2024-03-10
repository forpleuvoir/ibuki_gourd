package moe.forpleuvoir.ibukigourd.render.math.bezier

import kotlin.math.pow

object QuartEasing : Easing {

    override fun easeIn(t: Float): Float = t * t * t * t

    override fun easeOut(t: Float): Float = 1f - (1f - t).pow(4f)

    override fun easeInOut(t: Float): Float = if (t < 0.5f) 8f * t * t * t * t else 1f - (-2f * t + 2f).pow(4f) / 2f

}