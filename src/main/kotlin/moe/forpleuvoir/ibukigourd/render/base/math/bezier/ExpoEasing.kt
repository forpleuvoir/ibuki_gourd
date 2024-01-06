package moe.forpleuvoir.ibukigourd.render.base.math.bezier

import moe.forpleuvoir.nebula.common.pick
import kotlin.math.pow

object ExpoEasing : Easing {
    override fun easeIn(t: Float): Float = (t == 0f).pick(0f, 2f.pow(10f * t - 10))
    override fun easeOut(t: Float): Float = (t == 1f).pick(1f, 1f - 2f.pow(-10f * t))

    override fun easeInOut(t: Float): Float =
        when {
            t == 0f  -> 0f
            t == 1f  -> 1f
            t < 0.5f -> 2f.pow(20f * t - 10f) / 2f
            else     -> (2f - 2f.pow(-20f * t + 10f)) / 2f
        }

}