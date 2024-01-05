package moe.forpleuvoir.ibukigourd.render.base.math.bezier

import moe.forpleuvoir.nebula.common.pick
import kotlin.math.pow

object BackEasing : Easing {
    override fun easeIn(t: Float): Float {
        val c1 = 1.70158f
        val c3 = c1 + 1f
        return c3 * t * t * t - c1 * t * t
    }

    override fun easeOut(t: Float): Float {
        val c1 = 1.70158f
        val c3 = c1 + 1f
        return 1f + c3 * (t - 1f).pow(3) + c1 * (t - 1).pow(2)
    }

    override fun easeInOut(t: Float): Float {
        val c1 = 1.70158f
        val c2 = c1 * 1.525f

        return (t < 0.5f).pick(
            ((2f * t).pow(2f) * ((c2 + 1f) * 2f * t - c2)) / 2f,
            ((2f * t - 2f).pow(2f) * ((c2 + 1f) * (t * 2f - 2f) + c2) + 2f) / 2f
        )

    }
}