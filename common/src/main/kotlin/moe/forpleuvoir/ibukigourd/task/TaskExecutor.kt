package moe.forpleuvoir.ibukigourd.task

import moe.forpleuvoir.nebula.serialization.Serializable


interface TaskExecutor<T> : Serializable {

    fun asString(): String {
        return runCatching {
            this::class.simpleName!!
        }.getOrDefault("anonymous_task")

    }

    fun execute(task: TickTask<T>, context: T)

    companion object {

        fun <T, E : TaskExecutor<T>> E.createTask(delay: Int = 0, period: Int = 1, times: Int = 1): TickTask<T> = TickTask(delay, period, times, this)

    }

}