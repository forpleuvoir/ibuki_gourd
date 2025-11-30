package moe.forpleuvoir.ibukigourd

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(IbukiGourd.MOD_ID, dist = [Dist.CLIENT])
class NeoforgeIbukigourdClient(eventBus: IEventBus, modContainer: ModContainer) {
    init {
        IbukiGourdClient.init()
    }
}