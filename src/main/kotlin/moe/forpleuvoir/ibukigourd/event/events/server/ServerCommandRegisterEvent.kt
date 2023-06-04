package moe.forpleuvoir.ibukigourd.event.events.server

import com.mojang.brigadier.CommandDispatcher
import moe.forpleuvoir.nebula.event.Event
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

/**
 * 服务端命令注册事件
 * @property dispatcher CommandDispatcher<ServerCommandSource>
 * @property registryAccess CommandRegistryAccess
 * @property environment RegistrationEnvironment
 * @constructor
 */
class ServerCommandRegisterEvent(
	@JvmField
	val dispatcher: CommandDispatcher<ServerCommandSource>,
	@JvmField
	val registryAccess: CommandRegistryAccess,
	@JvmField
	val environment: CommandManager.RegistrationEnvironment,
) : Event