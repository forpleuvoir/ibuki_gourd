package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.event.events.client.ClientCommandRegisterEvent
import moe.forpleuvoir.ibukigourd.fabricevent.ReloadListenerRegistry
import moe.forpleuvoir.nebula.event.EventBus
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object FabricIbukiGourd : ModInitializer {

    override fun onInitialize() {
        IbukiGourd.init()
        ReloadListenerRegistry.register()
        registerClientCommand()
    }

    fun registerClientCommand() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, buildContext ->
            EventBus.broadcast(ClientCommandRegisterEvent(dispatcher, buildContext))
        }
    }

}