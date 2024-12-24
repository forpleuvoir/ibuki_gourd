package moe.forpleuvoir.ibukigourd.task

import moe.forpleuvoir.nebula.serialization.Serializable


interface TaskExecutor<T> : Serializable {

    val asString: String get() = this::class.simpleName ?: "anonymous"

    fun execute(task: TickTask<T>, context: T)

    fun createTask(delay: Int = 0, period: Int = 1, times: Int = 1): TickTask<T> = TickTask(delay, period, times, this)

    companion object

}