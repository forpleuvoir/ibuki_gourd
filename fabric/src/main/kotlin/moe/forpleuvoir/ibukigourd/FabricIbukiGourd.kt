package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.event.events.server.ServerCommandRegistrationEvent
import moe.forpleuvoir.nebula.event.invoke
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

object FabricIbukiGourd : ModInitializer {

    override fun onInitialize() {
        IbukiGourd.init()
        CommandRegistrationCallback.EVENT.register { dispatcher, context, selection ->
            ServerCommandRegistrationEvent()(dispatcher, context, selection)
        }
    }

}