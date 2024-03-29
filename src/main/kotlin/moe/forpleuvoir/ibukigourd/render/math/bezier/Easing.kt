package moe.forpleuvoir.ibukigourd.render.math.bezier

import org.jetbrains.annotations.Contract

/**
 * 缓动映射函数
 */
typealias Ease = (Float) -> Float

/**
 * https://easings.net/zh-cn
 */
interface Easing {

    @Suppress("NOTHING_TO_INLINE")
    companion object {

        val LINEAR: Easing = object : Easing {
            override fun easeIn(t: Float): Float = t

            override fun easeOut(t: Float): Float = t

            override fun easeInOut(t: Float): Float = t
        }

        /**
         * 缓入函数
         * @param easing Easing 缓动函数实现
         * @param t Float 时间  0f..1.0f
         * @return Float 缓动后的值
         */
        @Contract(pure = true)
        inline fun easeIn(easing: Easing, t: Float): Float {
            return easing.easeIn(t)
        }

        /**
         * 缓出函数
         * @param easing Easing 缓动函数实现
         * @param t Float 时间  0f..1.0f
         * @return Float 缓动后的值
         */
        @Contract(pure = true)
        inline fun easeOut(easing: Easing, t: Float): Float {
            return easing.easeOut(t)
        }

        /**
         * 缓入缓出函数
         * @param easing Easing 缓动函数实现
         * @param t Float 时间  0f..1.0f
         * @return Float 缓动后的值
         */
        @Contract(pure = true)
        inline fun easeInOut(easing: Easing, t: Float): Float {
            return easing.easeInOut(t)
        }

    }

    /**
     * 缓入函数
     * @param t Float 时间  0f..1.0f
     * @return Float 缓动后的值
     */
    @Contract(pure = true)
    fun easeIn(t: Float): Float

    /**
     * 缓出函数
     * @param t Float 时间  0f..1.0f
     * @return Float 缓动后的值
     */
    @Contract(pure = true)
    fun easeOut(t: Float): Float

    /**
     * 缓入缓出函数
     * @param t Float 时间  0f..1.0f
     * @return Float 缓动后的值
     */
    @Contract(pure = true)
    fun easeInOut(t: Float): Float

}