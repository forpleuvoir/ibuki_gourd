package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.event.events.client.ClientCommandRegisterEvent
import moe.forpleuvoir.nebula.event.EventBus
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object FabricIbukiGourdClient : ClientModInitializer {

    override fun onInitializeClient() {
        IbukiGourdClient.init()
        registerClientCommand()
    }

    fun registerClientCommand() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, buildContext ->
            EventBus.broadcast(ClientCommandRegisterEvent(dispatcher, buildContext))
        }
    }
}