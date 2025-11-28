package moe.forpleuvoir.ibukigourd.task

import net.minecraft.server.MinecraftServer
import java.util.concurrent.ConcurrentLinkedQueue

open class TickTaskScheduler<T> {

    @Suppress("NOTHING_TO_INLINE")
    companion object {
        @JvmStatic
        val Server by lazy { TickTaskScheduler<MinecraftServer>() }

        //------------ START ------------\\

        inline fun MinecraftServer.scheduleStartTick(task: TickTask<MinecraftServer>) =
            Server.scheduleStartTick(task)

        inline fun MinecraftServer.scheduleStartTick(noinline action: (TickTask<MinecraftServer>, MinecraftServer) -> Unit) =
            Server.scheduleStartTick(action)

        inline fun MinecraftServer.scheduleStopTick(crossinline task: () -> Unit) =
            Server.scheduleStartTick { _, _ -> task() }

        inline fun MinecraftServer.scheduleStartTick(delay: Int = 0, noinline action: (TickTask<MinecraftServer>, MinecraftServer) -> Unit) =
            Server.scheduleStartTick(delay, action)

        inline fun MinecraftServer.scheduleStopTick(delay: Int = 0, crossinline task: () -> Unit) =
            Server.scheduleStartTick(delay) { _, _ -> task() }

        //------------ END ------------\\

        inline fun MinecraftServer.scheduleEndTick(task: TickTask<MinecraftServer>) =
            Server.scheduleEndTick(task)

        inline fun MinecraftServer.scheduleEndTick(noinline action: (TickTask<MinecraftServer>, MinecraftServer) -> Unit) =
            Server.scheduleEndTick(action)

        inline fun MinecraftServer.scheduleEndTick(crossinline task: () -> Unit) =
            Server.scheduleEndTick { _, _ -> task() }

        inline fun MinecraftServer.scheduleEndTick(delay: Int = 0, noinline action: (TickTask<MinecraftServer>, MinecraftServer) -> Unit) =
            Server.scheduleEndTick(delay, action)

        inline fun MinecraftServer.scheduleEndTick(delay: Int = 0, crossinline task: () -> Unit) =
            Server.scheduleEndTick(delay) { _, _ -> task() }

    }

    private val _startTasks = ConcurrentLinkedQueue<TickTask<T>>()
    private val _startRemoveList = ConcurrentLinkedQueue<TickTask<T>>()
    private val _endTasks = ConcurrentLinkedQueue<TickTask<T>>()
    private val _endRemoveList = ConcurrentLinkedQueue<TickTask<T>>()

    val tasks get() = _startTasks + _endTasks
    val startTasks get() = _startTasks.toList()
    val endTasks get() = _endTasks.toList()

    fun scheduleStartTick(task: TickTask<T>) {
        addToStart(task)
    }

    fun scheduleStartTick(action: (TickTask<T>, T) -> Unit) {
        addToStart(TickTask(0, 1, 1, action))
    }

    fun scheduleStartTick(delay: Int = 0, action: (TickTask<T>, T) -> Unit) {
        addToStart(TickTask(delay, 1, 1, action))
    }

    fun scheduleEndTick(task: TickTask<T>) {
        addToEnd(task)
    }

    fun scheduleEndTick(action: (TickTask<T>, T) -> Unit) {
        addToEnd(TickTask(0, 1, 1, action))
    }

    fun scheduleEndTick(delay: Int = 0, action: (TickTask<T>, T) -> Unit) {
        addToEnd(TickTask(delay, 1, 1, action))
    }

    protected open fun addToStart(task: TickTask<T>) {
        _startTasks.add(task)
    }

    protected open fun addToEnd(task: TickTask<T>) {
        _endTasks.add(task)
    }

    fun remove(task: TickTask<T>) {
        removeFromStart(task)
        removeFromEnd(task)
    }

    protected open fun removeFromStart(task: TickTask<T>) {
        _startRemoveList.add(task)
    }

    protected open fun removeFromEnd(task: TickTask<T>) {
        _endRemoveList.add(task)
    }

    fun clear() {
        _startTasks.clear()
        _endTasks.clear()
    }

    private fun endRemoveHandler() {
        _endRemoveList.forEach {
            _endTasks.remove(it)
        }
        _endRemoveList.clear()
    }

    private fun startRemoveHandler() {
        _startRemoveList.forEach {
            _startTasks.remove(it)
        }
        _startRemoveList.clear()
    }


    fun startTick(context: T) {
        startRemoveHandler()
        val iterator = _startTasks.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            if (task.isOver) {
                removeFromStart(task)
            } else {
                task.tryExecute(context)
            }
        }
    }

    fun endTick(context: T) {
        endRemoveHandler()
        val iterator = _endTasks.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            if (task.isOver) {
                removeFromEnd(task)
            } else {
                task.tryExecute(context)
            }
        }
    }

}