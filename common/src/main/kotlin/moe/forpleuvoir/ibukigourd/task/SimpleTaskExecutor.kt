package moe.forpleuvoir.ibukigourd.task

import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeNull

fun interface SimpleTaskExecutor<T> : TaskExecutor<T> {

    override fun execute(task: TickTask<T>, context: T)

    override fun serialization(): SerializeElement = SerializeNull

}