package moe.forpleuvoir.ibukigourd.render.math.bezier

import kotlin.math.pow
import kotlin.math.sin

object ElasticEasing : Easing {
    override fun easeIn(t: Float): Float {
        val c4 = (2 * Math.PI).toFloat() / 3f
        return when (t) {
            0f   -> 0f
            1f   -> 1f
            else -> -(2f.pow(10f * t - 10.0f)) * sin((t * 10f - 10.75f) * c4.toDouble()).toFloat()
        }
    }

    override fun easeOut(t: Float): Float {
        val c4 = (2f * Math.PI).toFloat() / 3f
        return when (t) {
            0f   -> 0f
            1f   -> 1f
            else -> 2f.pow(-10f * t) * sin((t * 10f - 0.75f) * c4.toDouble()).toFloat() + 1f
        }
    }

    override fun easeInOut(t: Float): Float {
        val c5 = (2f * Math.PI).toFloat() / 4.5f
        return when (t) {
            0f   -> 0f
            1f   -> 1f
            else -> if (t < 0.5f) -(2f.pow(20f * t - 10f)) * sin((20f * t - 11.125f) * c5.toDouble()).toFloat() / 2f else (2f.pow(-20f * t + 10f) * sin((20f * t - 11.125f) * c5.toDouble()).toFloat() / 2f + 1f)
        }
    }
}