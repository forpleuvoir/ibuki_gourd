package com.forpleuvoir.ibukigourd

import com.forpleuvoir.ibukigourd.event.events.client.ClientCommandRegisterEvent
import com.forpleuvoir.ibukigourd.util.logger
import com.forpleuvoir.nebula.event.EventBus
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

@Environment(EnvType.CLIENT)
object IbukiGourdClient : ClientModInitializer {

	private val log = logger()

	override fun onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess ->
			log.info("client command register...")
			EventBus.broadcast(ClientCommandRegisterEvent(dispatcher, registryAccess))
		}
	}

}