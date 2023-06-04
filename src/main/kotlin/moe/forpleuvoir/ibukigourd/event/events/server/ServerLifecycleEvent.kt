package moe.forpleuvoir.ibukigourd.event.events.server

import moe.forpleuvoir.nebula.event.Event
import net.minecraft.server.MinecraftServer

/**
 * 服务端生命周期事件
 */
class ServerLifecycleEvent {

	/**
	 * 服务端启动完成事件
	 * @property server MinecraftServer
	 * @constructor
	 */
	class ServerStartedEvent(@JvmField val server: MinecraftServer) : Event

	/**
	 * 服务端启动中事件
	 * @property server MinecraftServer
	 * @constructor
	 */
	class ServerStartingEvent(@JvmField val server: MinecraftServer) : Event

	/**
	 * 服务端关闭事件
	 * @property server MinecraftServer
	 * @constructor
	 */
	class ServerStoppedEvent(@JvmField val server: MinecraftServer) : Event

	/**
	 * 服务端关闭中事件
	 * @property server MinecraftServer
	 * @constructor
	 */
	class ServerStoppingEvent(@JvmField val server: MinecraftServer) : Event

}