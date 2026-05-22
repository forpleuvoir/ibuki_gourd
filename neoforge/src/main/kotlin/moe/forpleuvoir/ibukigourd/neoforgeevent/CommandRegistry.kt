package moe.forpleuvoir.ibukigourd.neoforgeevent

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.event.events.client.ClientCommandRegistrationEvent
import moe.forpleuvoir.ibukigourd.event.events.server.ServerCommandRegistrationEvent
import moe.forpleuvoir.nebula.event.invoke
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent

@EventBusSubscriber(modid = IbukiGourd.MOD_ID)
object CommandRegistry {

    @SubscribeEvent
    fun onRegisterClientCommand(event: RegisterClientCommandsEvent) {
        ClientCommandRegistrationEvent()(event.dispatcher, event.buildContext)
    }

    @SubscribeEvent
    fun onRegisterClientCommand(event: RegisterCommandsEvent) {
        ServerCommandRegistrationEvent()(event.dispatcher, event.buildContext, event.commandSelection)
    }

}