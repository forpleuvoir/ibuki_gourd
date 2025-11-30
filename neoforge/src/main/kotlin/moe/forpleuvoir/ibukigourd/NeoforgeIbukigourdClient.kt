package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.mod.gui.IbukiGourdModScreen
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

@Mod(IbukiGourd.MOD_ID, dist = [Dist.CLIENT])
class NeoforgeIbukigourdClient(eventBus: IEventBus, modContainer: ModContainer) {
    init {
        IbukiGourdClient.init()
        //模组菜单配置接口
        modContainer.registerExtensionPoint(
            IConfigScreenFactory::class.java,
            IConfigScreenFactory { _, modListScreen -> IbukiGourdModScreen().apply { parentScreen = modListScreen } }
        )
    }
}