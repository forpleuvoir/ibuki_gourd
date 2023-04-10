package com.forpleuvoir.ibukigourd

import com.forpleuvoir.ibukigourd.event.events.ModInitializerEvent
import com.forpleuvoir.ibukigourd.event.events.server.ServerCommandRegisterEvent
import com.forpleuvoir.ibukigourd.util.loader
import com.forpleuvoir.ibukigourd.util.logger
import com.forpleuvoir.nebula.event.EventBus
import com.llamalad7.mixinextras.MixinExtrasBootstrap
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

object IbukiGourd : ModInitializer {

	val log = logger()

	const val MOD_ID: String = "ibukigourd"

	const val MOD_NAME: String = "Ibuki Gourd"

	override fun onInitialize() {
		MixinExtrasBootstrap.init()
		loader.getModContainer(MOD_ID).ifPresent {
			EventBus.broadcast(ModInitializerEvent(it.metadata))
		}
		CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
			EventBus.broadcast(ServerCommandRegisterEvent(dispatcher, registryAccess, environment))
		}
	}

}