package com.forpleuvoir.ibukigourd.event.events.client

import com.forpleuvoir.nebula.event.Event
import net.minecraft.client.MinecraftClient

/**
 * 客户端tick事件
 */
class ClientTickEvent {

	/**
	 * 客户端tick结束事件

	 * @property minecraftClient Minecraft
	 * @constructor
	 */
	class ClientTickEndEvent(@JvmField val minecraftClient: MinecraftClient) : Event

	/**
	 * 客户端tick开始事件
	 * @property minecraftClient Minecraft
	 * @constructor
	 */
	class ClientTickStartEvent(@JvmField val minecraftClient: MinecraftClient) : Event

}