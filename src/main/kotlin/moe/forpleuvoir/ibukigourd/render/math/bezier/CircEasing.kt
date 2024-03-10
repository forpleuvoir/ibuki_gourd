package moe.forpleuvoir.ibukigourd.render.math.bezier

import kotlin.math.pow
import kotlin.math.sqrt

object CircEasing : Easing {

    override fun easeIn(t: Float): Float = 1f - sqrt(1f - t.pow(2f))

    override fun easeOut(t: Float): Float = sqrt(1f - (t - 1).pow(2))

    override fun easeInOut(t: Float): Float = if (t < 0.5f) ((1f - sqrt(1f - (2f * t).pow(2f))) / 2f) else ((sqrt(1f - (-2f * t + 2f).pow(2f)) + 1f) / 2f)

}