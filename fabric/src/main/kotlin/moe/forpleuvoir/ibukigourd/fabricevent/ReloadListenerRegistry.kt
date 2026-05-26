package moe.forpleuvoir.ibukigourd.fabricevent

import moe.forpleuvoir.ibukigourd.IbukiGourdClient
import net.fabricmc.fabric.api.resource.v1.ResourceLoader
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.PreparableReloadListener

object ReloadListenerRegistry {

    fun registerClientResource() {
        IbukiGourdClient.addClientResourceReloaderListener { listener ->
            ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(listener.identifier, listener)
        }
    }

}