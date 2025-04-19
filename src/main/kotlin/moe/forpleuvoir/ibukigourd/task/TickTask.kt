package moe.forpleuvoir.ibukigourd.task

import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.extensions.checkType
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject


class TickTask<T>(
    val setting: Setting,
    private val executor: TaskExecutor<T>
) : Serializable {

    companion object {
        fun <T> fromTask(task: TickTask<T>) = TickTask(task.delay, task.period, task.times, task.executor)
    }

    constructor(delay: Int = 0, period: Int = 1, times: Int = 1, executor: TaskExecutor<T>) :
            this(Setting(delay, period, times), executor)

    constructor(delay: Int = 0, period: Int = 1, times: Int = 1, action: (TickTask<T>, T) -> Unit) :
            this(delay, period, times, SimpleTaskExecutor(action))

    data class Setting(val delay: Int = 0, val period: Int = 1, val times: Int = 1) : Serializable {
        override fun serialization(): SerializeElement = serializeObject {
            "delay" to delay
            "period" to period
            "times" to times
        }

        companion object : Deserializer<Setting> {
            override fun deserialization(serializeElement: SerializeElement): Setting =
                serializeElement.checkType<SerializeObject, Setting> {
                    Setting(
                        it["delay"]!!.asInt.coerceAtLeast(0),
                        it["period"]!!.asInt.coerceAtLeast(1),
                        it["times"]!!.asInt.coerceAtLeast(1)
                    )
                }.getOrDefault(Setting())
        }
    }

    init {
        check(delay >= 0) { "delay must be >=0" }
        check(period >= 1) { "period must be >=1" }
        check(times >= 1) { "period must be >=1" }
    }

    val delay: Int get() = setting.delay
    val period: Int get() = setting.period
    val times: Int get() = setting.times

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
        "setting" to setting.serialization()
        "executor" to executor.serialization()
    }

    override fun toString(): String {
        return "TickTask(delay=$delay, period=$period, times=$times, executor=$executor)"
    }

}