package moe.forpleuvoir.ibukigourd.util.animation

import moe.forpleuvoir.ibukigourd.util.math.bezier.Ease
import kotlin.time.Duration

interface Animator {

    /**
     * 动画时长
     */
    val duration: Duration

    /**
     * 动画结束时的回调
     */
    var onEnd: () -> Unit

    /**
     * 动画开始时的回调（必须是从头开始播放才调用）
     */
    var onStart: () -> Unit

    /**
     * 缓动函数
     */
    val easing: Ease

    /**
     * 开始动画
     *
     * 如果动画已暂停，则继续播放
     *
     * 如果动画已结束，则重新开始
     */
    fun play()

    /**
     * 暂停动画
     */
    fun pause()

    /**
     * 停止动画
     */
    fun stop()

    /**
     * 更新动画
     */
    fun update()

    /**
     * 重放动画（停止并重新开始）
     */
    fun replay()

    /**
     * 重置动画至初始状态（清除进度并重设值）
     */
    fun reset()

}
