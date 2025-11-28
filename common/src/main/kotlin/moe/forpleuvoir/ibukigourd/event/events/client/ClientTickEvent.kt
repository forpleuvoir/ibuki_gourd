package moe.forpleuvoir.ibukigourd.event.events.client

import moe.forpleuvoir.nebula.event.Event
import net.minecraft.client.Minecraft

/**
 * 客户端tick事件
 */
class ClientTickEvent {

	/**
	 * 客户端tick结束事件

	 * @property minecraftClient Minecraft
	 */
    class ClientTickEndEvent(@JvmField val minecraftClient: Minecraft) : Event

	/**
	 * 客户端tick开始事件
	 * @property minecraftClient Minecraft
	 */
    class ClientTickStartEvent(@JvmField val minecraftClient: Minecraft) : Event

}