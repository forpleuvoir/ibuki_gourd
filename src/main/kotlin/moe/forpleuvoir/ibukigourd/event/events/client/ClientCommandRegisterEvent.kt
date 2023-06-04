package moe.forpleuvoir.ibukigourd.event.events.client

import com.mojang.brigadier.CommandDispatcher
import moe.forpleuvoir.nebula.event.Event
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.command.CommandRegistryAccess

class ClientCommandRegisterEvent(
	@JvmField val dispatcher: CommandDispatcher<FabricClientCommandSource>,
	@JvmField val registryAccess: CommandRegistryAccess,
) : Event