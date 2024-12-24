package moe.forpleuvoir.ibukigourd.task

import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject


data class TickTask<T>(
    val delay: Int = 0,
    val period: Int = 1,
    val times: Int = 1,
    private val executor: TaskExecutor<T>
) : Serializable {

    companion object {
        fun <T> from(task: TickTask<T>) = TickTask(task.delay, task.period, task.times, task.executor)
    }

    constructor(delay: Int, period: Int, times: Int, action: (TickTask<T>, T) -> Unit) :
            this(delay, period, times, SimpleTaskExecutor(action))

    init {
        check(delay >= 0) { "delay must be >=0" }
        check(period >= 1) { "period must be >=1" }
        check(times >= 1) { "period must be >=1" }
    }

    var counter: Int = 0
        private set(value) {
            field = value.coerceIn(0, times)
        }

    private var tickCounter: Int = 0

    private val shouldExecute: Boolean
        get() {
            tickCounter++
            return if (counter == 0) {
                tickCounter >= delay
            } else {
                tickCounter >= period
            }
        }

    val isOver: Boolean get() = counter >= times

    fun tryExecute(context: T) {
        if (isOver || !shouldExecute) return
        executor.execute(this, context)
        counter++
        tickCounter = 0
    }


    override fun serialization(): SerializeElement = serializeObject {
        "delay" to delay
        "period" to period
        "times" to times
        "executor" to executor.serialization()
    }

    override fun toString(): String {
        return "TickTask(delay=$delay, period=$period, times=$times, executor=$executor)"
    }

}