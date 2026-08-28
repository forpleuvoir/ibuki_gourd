package moe.forpleuvoir.ibukigourd

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent

@Mod(IbukiGourd.MOD_ID, dist = [Dist.CLIENT])
class NeoforgeIbukigourdClient(eventBus: IEventBus, modContainer: ModContainer) {
    init {
        eventBus.addListener(::setup)
        // 配置界面已随 Compose Desktop UI 一并移除，待 compose-minecraft UI 落地后
        // 在此重新注册 IConfigScreenFactory
    }

    private fun setup(event: FMLClientSetupEvent) {
        IbukiGourdClient.init()
    }
}