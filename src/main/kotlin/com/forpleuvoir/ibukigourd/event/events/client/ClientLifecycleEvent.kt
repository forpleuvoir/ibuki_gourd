package com.forpleuvoir.ibukigourd.event.events.client

import com.forpleuvoir.nebula.event.Event
import net.minecraft.client.MinecraftClient

/**
 * 客户端生命周期事件
 */
class ClientLifecycleEvent {

	/**
	 * 客户端启动完成事件
	 * @property minecraftClient Minecraft
	 * @constructor
	 */
	class ClientStartedEvent(@JvmField val minecraftClient: MinecraftClient) : Event

	/**
	 * 客户端启动中事件
	 * @property minecraftClient Minecraft
	 * @constructor
	 */
	class ClientStartingEvent(@JvmField val minecraftClient: MinecraftClient) : Event

	/**
	 * 客户端关闭事件
	 * @property minecraftClient Minecraft
	 * @constructor
	 */
	class ClientStopEvent(@JvmField val minecraftClient: MinecraftClient) : Event

}