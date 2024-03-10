package moe.forpleuvoir.ibukigourd.render.math.bezier

import moe.forpleuvoir.nebula.common.pick

object BounceEasing : Easing {
    override fun easeIn(t: Float): Float {
        return 1f - easeOut(1f - t)
    }

    override fun easeOut(t: Float): Float {
        val n1: Float = 7.5625f
        val d1: Float = 2.75f

        return when {
            t < 1f / d1   -> n1 * t * t
            t < 2f / d1   -> n1 * (t - 1.5f / d1) * (t - 1.5f / d1) + 0.75f
            t < 2.5f / d1 -> n1 * (t - 2.25f / d1) * (t - 2.25f / d1) + 0.9375f
            else          -> n1 * (t - 2.625f / d1) * (t - 2.625f / d1) + 0.984375f
        }
    }

    override fun easeInOut(t: Float): Float {
        return (t < 0.5f).pick(
            (1f - easeOut(1f - 2f * t)) / 2f,
            (1f + easeOut(2f * t - 1f)) / 2f
        )

    }
}