package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.mod.ui.IbukiGourdScreen
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
        //模组菜单配置接口
        modContainer.registerExtensionPoint(
            IConfigScreenFactory::class.java,
            IConfigScreenFactory { _, modListScreen ->
                IbukiGourdScreen(parentScreen = modListScreen)
            }
        )
    }

    private fun setup(event: FMLClientSetupEvent) {
        IbukiGourdClient.init()
    }
}