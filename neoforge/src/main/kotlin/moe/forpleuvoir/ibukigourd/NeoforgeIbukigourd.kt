package moe.forpleuvoir.ibukigourd

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(IbukiGourd.MOD_ID)
class NeoforgeIbukigourd(eventBus: IEventBus, modContainer: ModContainer) {

    init {
        IbukiGourd.init()
    }

}