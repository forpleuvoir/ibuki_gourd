package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.event.IbukiGourdEventManager
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent

@Mod(IbukiGourd.MOD_ID)
class NeoforgeIbukigourd(eventBus: IEventBus, modContainer: ModContainer) {

    init {
        IbukiGourdEventManager.init()
        eventBus.addListener(::setup)
    }

    private fun setup(event: FMLCommonSetupEvent) {
        IbukiGourd.init()
    }

}