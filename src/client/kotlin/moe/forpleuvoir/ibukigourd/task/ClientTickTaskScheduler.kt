@file:Suppress("NOTHING_TO_INLINE")

package moe.forpleuvoir.ibukigourd.task

import net.minecraft.client.MinecraftClient

object ClientTickTaskScheduler : TickTaskScheduler<MinecraftClient>()

val MinecraftClient.tickTaskScheduler get() = ClientTickTaskScheduler

inline fun MinecraftClient.scheduleStartTick(task: TickTask<MinecraftClient>) =
    tickTaskScheduler.scheduleStartTick(task)

inline fun MinecraftClient.scheduleStartTick(noinline action: (MinecraftClient) -> Unit) =
    tickTaskScheduler.scheduleStartTick(action)

inline fun MinecraftClient.scheduleStartTick(delay: Int = 0, noinline action: (MinecraftClient) -> Unit) =
    tickTaskScheduler.scheduleStartTick(delay, action)

inline fun MinecraftClient.scheduleEndTick(task: TickTask<MinecraftClient>) =
    tickTaskScheduler.scheduleEndTick(task)

inline fun MinecraftClient.scheduleEndTick(noinline action: (MinecraftClient) -> Unit) =
    tickTaskScheduler.scheduleEndTick(action)

inline fun MinecraftClient.scheduleEndTick(delay: Int = 0, noinline action: (MinecraftClient) -> Unit) =
    tickTaskScheduler.scheduleEndTick(delay, action)