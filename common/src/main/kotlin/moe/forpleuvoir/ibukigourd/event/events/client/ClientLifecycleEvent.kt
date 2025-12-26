package moe.forpleuvoir.ibukigourd.event.events.client

import moe.forpleuvoir.nebula.event.Event
import net.minecraft.client.Minecraft

/**
 * 客户端生命周期事件
 */
class ClientLifecycleEvent {

    /**
     * 客户端启动中事件
     * @property minecraftClient Minecraft
     */
    class ClientStartingEvent(@JvmField val minecraftClient: Minecraft) : Event

    /**
     * 客户端关闭事件
     * @property minecraftClient Minecraft
     */
    class ClientStopEvent(@JvmField val minecraftClient: Minecraft) : Event

}