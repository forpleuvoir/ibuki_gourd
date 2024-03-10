package moe.forpleuvoir.ibukigourd.render.math.bezier

import kotlin.math.cos
import kotlin.math.sin

object SineEasing : Easing {
    override fun easeIn(t: Float): Float = 1f - cos((t * Math.PI) / 2f).toFloat()

    override fun easeOut(t: Float): Float = sin((t * Math.PI) / 2f).toFloat()

    override fun easeInOut(t: Float): Float = -(cos(Math.PI * t).toFloat() - 1f) / 2f

}