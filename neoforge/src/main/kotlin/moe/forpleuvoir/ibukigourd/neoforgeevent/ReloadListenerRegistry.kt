package moe.forpleuvoir.ibukigourd.neoforgeevent

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.IbukiGourdClient
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent

@EventBusSubscriber(modid = IbukiGourd.MOD_ID)
object ReloadListenerRegistry {

//    @SubscribeEvent
//    fun onAddServerReloadListeners(event: AddServerReloadListenersEvent) {
//
//    }

    @SubscribeEvent
    fun onAddClientReloadListeners(event: AddClientReloadListenersEvent) {
        IbukiGourdClient.addClientResourceReloaderListener { listener ->
            event.addListener(listener.identifier, listener)
        }
    }

}