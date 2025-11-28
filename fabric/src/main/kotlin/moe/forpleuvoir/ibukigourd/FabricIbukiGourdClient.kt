package moe.forpleuvoir.ibukigourd

import net.fabricmc.api.ClientModInitializer

object FabricIbukiGourdClient : ClientModInitializer {

    override fun onInitializeClient() {
        IbukiGourdClient.init()


    }
}