package moe.forpleuvoir.ibukigourd.fabricevent

import moe.forpleuvoir.ibukigourd.gui.base.widget.WidgetTextures
import moe.forpleuvoir.ibukigourd.gui.widget.icon.IconTextures
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object ReloadListenerRegistry {

    fun registerClientResource() {
        registerClientResource(WidgetTextures.RESOURCE_ID, WidgetTextures)
        registerClientResource(IconTextures.RESOURCE_ID, IconTextures)
    }

    private fun registerClientResource(id: ResourceLocation, listener: PreparableReloadListener) {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(IGResourceReloadListener(id, listener))
    }

    private class IGResourceReloadListener(
        val id: ResourceLocation,
        val listener: PreparableReloadListener
    ) : IdentifiableResourceReloadListener {
        override fun getFabricId(): ResourceLocation = id

        override fun reload(
            barrier: PreparableReloadListener.PreparationBarrier,
            manager: ResourceManager,
            backgroundExecutor: Executor,
            gameExecutor: Executor
        ): CompletableFuture<Void> {
            return listener.reload(barrier, manager, backgroundExecutor, gameExecutor)
        }
    }

}