package moe.forpleuvoir.ibukigourd

import com.llamalad7.mixinextras.MixinExtrasBootstrap
import moe.forpleuvoir.ibukigourd.event.events.ModInitializerEvent
import moe.forpleuvoir.ibukigourd.event.events.server.ServerCommandRegisterEvent
import moe.forpleuvoir.ibukigourd.util.loader
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.event.EventBus
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.metadata.ModMetadata

object IbukiGourd : ModInitializer {

	val log = logger()

	const val MOD_ID: String = "ibukigourd"

	const val MOD_NAME: String = "Ibuki Gourd"

	val metadata: ModMetadata get() = loader.getModContainer(MOD_ID).get().metadata

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