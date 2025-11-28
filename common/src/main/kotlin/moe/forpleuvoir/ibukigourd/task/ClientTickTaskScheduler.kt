@file:Suppress("NOTHING_TO_INLINE")

package moe.forpleuvoir.ibukigourd.task

import net.minecraft.client.Minecraft

val TickTaskScheduler.Companion.Client by lazy { TickTaskScheduler<Minecraft>() }

//------------ START ------------\\

inline fun Minecraft.scheduleStartTick(task: TickTask<Minecraft>) =
    TickTaskScheduler.Client.scheduleStartTick(task)

inline fun Minecraft.scheduleStartTick(noinline action: (TickTask<Minecraft>, Minecraft) -> Unit) =
    TickTaskScheduler.Client.scheduleStartTick(action)

inline fun Minecraft.scheduleStopTick(crossinline task: () -> Unit) =
    TickTaskScheduler.Client.scheduleStartTick { _, _ -> task() }

inline fun Minecraft.scheduleStartTick(delay: Int = 0, noinline action: (TickTask<Minecraft>, Minecraft) -> Unit) =
    TickTaskScheduler.Client.scheduleStartTick(delay, action)

inline fun Minecraft.scheduleStopTick(delay: Int = 0, crossinline task: () -> Unit) =
    TickTaskScheduler.Client.scheduleStartTick(delay) { _, _ -> task() }

//------------ END ------------\\

inline fun Minecraft.scheduleEndTick(task: TickTask<Minecraft>) =
    TickTaskScheduler.Client.scheduleEndTick(task)

inline fun Minecraft.scheduleEndTick(crossinline task: () -> Unit) =
    TickTaskScheduler.Client.scheduleEndTick { _, _ -> task() }

inline fun Minecraft.scheduleEndTick(noinline action: (TickTask<Minecraft>, Minecraft) -> Unit) =
    TickTaskScheduler.Client.scheduleEndTick(action)

inline fun Minecraft.scheduleEndTick(delay: Int = 0, noinline action: (TickTask<Minecraft>, Minecraft) -> Unit) =
    TickTaskScheduler.Client.scheduleEndTick(delay, action)

inline fun Minecraft.scheduleEndTick(delay: Int = 0, crossinline task: () -> Unit) =
    TickTaskScheduler.Client.scheduleEndTick(delay) { _, _ -> task() }