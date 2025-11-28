package moe.forpleuvoir.ibukigourd.neoforgeevent

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
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
        event.addListener(WidgetTextures.RESOURCE_ID, WidgetTextures)
        event.addListener(IconTextures.RESOURCE_ID, IconTextures)
    }

}