package moe.forpleuvoir.ibukigourd.task

import net.minecraft.server.MinecraftServer
import java.util.concurrent.ConcurrentLinkedQueue

open class TickTaskScheduler<T> {

    companion object {
        @JvmStatic
        val Server = TickTaskScheduler<MinecraftServer>()
    }

    private val startTasks = ConcurrentLinkedQueue<TickTask<T>>()
    private val startRemoveList = ConcurrentLinkedQueue<TickTask<T>>()
    private val endTasks = ConcurrentLinkedQueue<TickTask<T>>()
    private val endRemoveList = ConcurrentLinkedQueue<TickTask<T>>()

    fun scheduleStartTick(task: TickTask<T>) {
        startTasks.add(task)
    }

    fun scheduleStartTick(action: (T) -> Unit) {
        startTasks.add(TickTask(0, 1, 1, action))
    }

    fun scheduleStartTick(delay: Int = 0, action: (T) -> Unit) {
        startTasks.add(TickTask(delay, 1, 1, action))
    }

    fun scheduleEndTick(task: TickTask<T>) {
        endTasks.add(task)
    }

    fun scheduleEndTick(action: (T) -> Unit) {
        endTasks.add(TickTask(0, 1, 1, action))
    }

    fun scheduleEndTick(delay: Int = 0, action: (T) -> Unit) {
        endTasks.add(TickTask(delay, 1, 1, action))
    }

    fun remove(task: TickTask<T>) {
        startRemoveList.add(task)
        endRemoveList.add(task)
    }

    fun clear() {
        startTasks.clear()
        endTasks.clear()
    }

    private fun endRemoveHandler() {
        endRemoveList.forEach {
            endTasks.remove(it)
        }
        endRemoveList.clear()
    }

    private fun startRemoveHandler() {
        startRemoveList.forEach {
            startTasks.remove(it)
        }
        startRemoveList.clear()
    }


    fun startTick(context: T) {
        startRemoveHandler()
        val iterator = startTasks.iterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            if (value.isOver) {
                iterator.remove()
            } else {
                value.tryExecute(context)
            }
        }
    }

    fun endTick(context: T) {
        endRemoveHandler()
        val iterator = endTasks.iterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            if (value.isOver) {
                iterator.remove()
            } else {
                value.tryExecute(context)
            }
        }
    }

}