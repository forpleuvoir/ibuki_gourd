@file:Suppress("NOTHING_TO_INLINE")

package moe.forpleuvoir.ibukigourd.task

import net.minecraft.client.MinecraftClient

val TickTaskScheduler.Companion.Client by lazy { TickTaskScheduler<MinecraftClient>() }

//------------ START ------------\\

inline fun MinecraftClient.scheduleStartTick(task: TickTask<MinecraftClient>) =
    TickTaskScheduler.Client.scheduleStartTick(task)

inline fun MinecraftClient.scheduleStartTick(noinline action: (TickTask<MinecraftClient>, MinecraftClient) -> Unit) =
    TickTaskScheduler.Client.scheduleStartTick(action)

inline fun MinecraftClient.scheduleStopTick(crossinline task: () -> Unit) =
    TickTaskScheduler.Client.scheduleStartTick { _, _ -> task() }

inline fun MinecraftClient.scheduleStartTick(delay: Int = 0, noinline action: (TickTask<MinecraftClient>, MinecraftClient) -> Unit) =
    TickTaskScheduler.Client.scheduleStartTick(delay, action)

inline fun MinecraftClient.scheduleStopTick(delay: Int = 0, crossinline task: () -> Unit) =
    TickTaskScheduler.Client.scheduleStartTick(delay) { _, _ -> task() }

//------------ END ------------\\

inline fun MinecraftClient.scheduleEndTick(task: TickTask<MinecraftClient>) =
    TickTaskScheduler.Client.scheduleEndTick(task)

inline fun MinecraftClient.scheduleEndTick(crossinline task: () -> Unit) =
    TickTaskScheduler.Client.scheduleEndTick { _, _ -> task() }

inline fun MinecraftClient.scheduleEndTick(noinline action: (TickTask<MinecraftClient>, MinecraftClient) -> Unit) =
    TickTaskScheduler.Client.scheduleEndTick(action)

inline fun MinecraftClient.scheduleEndTick(delay: Int = 0, noinline action: (TickTask<MinecraftClient>, MinecraftClient) -> Unit) =
    TickTaskScheduler.Client.scheduleEndTick(delay, action)

inline fun MinecraftClient.scheduleEndTick(delay: Int = 0, crossinline task: () -> Unit) =
    TickTaskScheduler.Client.scheduleEndTick(delay) { _, _ -> task() }