package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.mod.ibukiGourdModScreen
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

@Mod(IbukiGourd.MOD_ID, dist = [Dist.CLIENT])
class NeoforgeIbukigourdClient(eventBus: IEventBus, modContainer: ModContainer) {
    init {
        eventBus.addListener(::setup)
        // 模组列表的"配置"按钮
        modContainer.registerExtensionPoint(
            IConfigScreenFactory::class.java,
            IConfigScreenFactory { _, modListScreen -> ibukiGourdModScreen(modListScreen) },
        )
    }

    private fun setup(event: FMLClientSetupEvent) {
        IbukiGourdClient.init()
    }
}