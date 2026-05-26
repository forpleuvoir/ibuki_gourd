package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.event.events.client.ClientCommandRegistrationEvent
import moe.forpleuvoir.ibukigourd.fabricevent.ReloadListenerRegistry
import moe.forpleuvoir.nebula.event.invoke
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object FabricIbukiGourdClient : ClientModInitializer {

    override fun onInitializeClient() {
        IbukiGourdClient.init()
        ReloadListenerRegistry.registerClientResource()
        registerClientCommand()

    }

    fun registerClientCommand() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, buildContext ->
            ClientCommandRegistrationEvent()(dispatcher, buildContext)
        }
    }
}