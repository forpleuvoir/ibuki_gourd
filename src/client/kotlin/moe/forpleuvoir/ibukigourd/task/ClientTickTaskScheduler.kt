@file:Suppress("NOTHING_TO_INLINE")

package moe.forpleuvoir.ibukigourd.task

import net.minecraft.client.MinecraftClient

val TickTaskScheduler.Companion.Client by lazy { TickTaskScheduler<MinecraftClient>() }

inline fun MinecraftClient.scheduleStartTick(task: TickTask<MinecraftClient>) =
    TickTaskScheduler.Client.scheduleStartTick(task)

inline fun MinecraftClient.scheduleStartTick(noinline action: (MinecraftClient) -> Unit) =
    TickTaskScheduler.Client.scheduleStartTick(action)

inline fun MinecraftClient.scheduleStartTick(delay: Int = 0, noinline action: (MinecraftClient) -> Unit) =
    TickTaskScheduler.Client.scheduleStartTick(delay, action)

inline fun MinecraftClient.scheduleEndTick(task: TickTask<MinecraftClient>) =
    TickTaskScheduler.Client.scheduleEndTick(task)

inline fun MinecraftClient.scheduleEndTick(noinline action: (MinecraftClient) -> Unit) =
    TickTaskScheduler.Client.scheduleEndTick(action)

inline fun MinecraftClient.scheduleEndTick(delay: Int = 0, noinline action: (MinecraftClient) -> Unit) =
    TickTaskScheduler.Client.scheduleEndTick(delay, action)