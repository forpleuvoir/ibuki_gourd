package moe.forpleuvoir.ibukigourd.util.animation

import moe.forpleuvoir.ibukigourd.util.math.bezier.Ease
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.asMutableState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

interface NumericAnimator<T> : Animator where  T : Number, T : Comparable<T> {

    val value: State<T>

    val targetValue: T

}

abstract class NumericAnimatorImpl<T>(
    value: T,
    override val targetValue: T,
    override val duration: Duration,
    override var onStart: () -> Unit = {},
    override var onEnd: () -> Unit = {},
    override val easing: Ease = { it }
) : NumericAnimator<T> where  T : Number, T : Comparable<T> {

    init {
        require(duration > 0.seconds) { "duration must be greater than 0" }
    }

    protected val initValue = value

    override val value: State<T> = value.asMutableState

    private var isRunning = false

    private var baseElapsed: Duration = 0.seconds

    private var runningStart: TimeSource.Monotonic.ValueTimeMark? = null

    override fun pause() {
        if (!isRunning) return
        baseElapsed += runningStart?.elapsedNow() ?: 0.seconds
        runningStart = null
        isRunning = false
    }

    override fun stop() {
        if (isRunning) {
            baseElapsed += runningStart?.elapsedNow() ?: 0.seconds
        }
        isRunning = false
        runningStart = null
    }

    override fun play() {
        if (isRunning) return
        val startingFromHead = if (baseElapsed >= duration) {
            baseElapsed = 0.seconds
            (value as MutableState).setValue(initValue)
            true
        } else {
            baseElapsed == 0.seconds
        }
        runningStart = TimeSource.Monotonic.markNow()
        isRunning = true
        if (startingFromHead) {
            onStart()
        }
    }

    override fun update() {
        if (!isRunning) return
        val elapsed = baseElapsed + (runningStart?.elapsedNow() ?: 0.seconds)
        val rawProgress = (elapsed / duration).coerceIn(0.0, 1.0)
        val progress = rawProgress.toFloat()
        (value as MutableState).setValue(applyValue(easing(progress)))
        if (rawProgress >= 1.0) {
            onEnd()
            isRunning = false
            baseElapsed = duration
        }

    }

    override fun replay() {
        reset()
        play()
    }

    override fun reset() {
        isRunning = false
        runningStart = null
        baseElapsed = 0.seconds
        (value as MutableState).setValue(initValue)
    }

    /**
     * 应用当前值
     * @param factor Float 插值因子
     */
    protected abstract fun applyValue(factor: Float): T

}

class FloatAnimator(
    value: Float,
    targetValue: Float,
    duration: Duration,
    onStart: () -> Unit = {},
    onEnd: () -> Unit = {},
    easing: Ease = { it }
) : NumericAnimatorImpl<Float>(
    value, targetValue, duration, onStart, onEnd, easing
) {
    override fun applyValue(factor: Float): Float {
        return initValue + ((targetValue - initValue) * factor)
    }
}

sealed class DoubleAnimator(
    value: Double,
    targetValue: Double,
    duration: Duration,
    onStart: () -> Unit = {},
    onEnd: () -> Unit = {},
    easing: Ease = { it }
) : NumericAnimatorImpl<Double>(
    value, targetValue, duration, onStart, onEnd, easing
) {
    override fun applyValue(factor: Float): Double {
        return initValue + ((targetValue - initValue) * factor)
    }
}

class ProgressAnimator(
    duration: Duration,
    onStart: () -> Unit = {},
    onEnd: () -> Unit = {},
    easing: Ease = { it }
) : DoubleAnimator(
    0.0, 1.0, duration, onStart, onEnd, easing
)
