package moe.forpleuvoir.ibukigourd.neoforgeevent

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.event.events.client.ClientCommandRegisterEvent
import moe.forpleuvoir.nebula.event.EventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent

@EventBusSubscriber(modid = IbukiGourd.MOD_ID)
object CommandRegistry {

    @SubscribeEvent
    fun onRegisterClientCommand(event: RegisterClientCommandsEvent) {
        EventBus.broadcast(ClientCommandRegisterEvent(event.dispatcher, event.buildContext))
    }

}