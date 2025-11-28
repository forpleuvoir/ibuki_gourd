package moe.forpleuvoir.ibukigourd

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.javafmlmod.FMLModContainer

@Mod(IbukiGourd.MOD_ID, dist = [Dist.CLIENT])
class NeoforgeIbukigourdClient(container: FMLModContainer, modBus: IEventBus, dist: Dist) {
    init {
        IbukiGourdClient.init()
    }
}