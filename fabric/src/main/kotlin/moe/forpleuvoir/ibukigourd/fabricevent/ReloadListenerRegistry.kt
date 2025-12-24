package moe.forpleuvoir.ibukigourd.fabricevent

import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import net.fabricmc.fabric.api.resource.v1.ResourceLoader
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.PreparableReloadListener

object ReloadListenerRegistry {

    fun register() {
        registerClientResource(WidgetTextures.RESOURCE_ID, WidgetTextures)
        registerClientResource(IconTextures.RESOURCE_ID, IconTextures)
    }

    private fun registerClientResource(id: Identifier, listener: PreparableReloadListener) {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(id, listener)
    }

    private fun registerServerData(id: Identifier, listener: PreparableReloadListener) {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloader(id, listener)
    }

}