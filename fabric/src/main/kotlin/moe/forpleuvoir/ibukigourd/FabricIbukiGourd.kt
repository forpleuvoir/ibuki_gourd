package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.fabricevent.ReloadListenerRegistry
import net.fabricmc.api.ModInitializer

object FabricIbukiGourd : ModInitializer {

    override fun onInitialize() {
        IbukiGourd.init()
        ReloadListenerRegistry.register()
    }

}