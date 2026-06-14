package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.api.ClientResourceReloaderListener
import moe.forpleuvoir.ibukigourd.config.ClientModConfigHandler
import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.ui.ComposeSceneWarmup
import moe.forpleuvoir.ibukigourd.ui.skia.SkiaContext
import moe.forpleuvoir.ibukigourd.ui.toast.ToastOverlayHost
import moe.forpleuvoir.ibukigourd.ui.preset.SkiaTextureHelper
import moe.forpleuvoir.ibukigourd.ui.util.render.SkiaItemRenderHelper
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.event.Event

object IbukiGourdClient {

    private val logger = logger()

    private val inits = listOf(
        ClientModConfigHandler
    )

    private val clientResourceReloaderListener = listOf<ClientResourceReloaderListener>(
        SkiaItemRenderHelper,
        SkiaTextureHelper
    )

    fun addClientResourceReloaderListener(listener: (ClientResourceReloaderListener) -> Unit) {
        clientResourceReloaderListener.forEach(listener)
    }


    fun init() {
        val initPhase = "${IbukiGourd.MOD_ID}:init"
        inits.forEach { it.init() }

        ClientModConfigHandler.register(IGConfig)

        ClientLifecycleEvent.Starting.register(initPhase) {
            SkiaContext.init()
            ComposeSceneWarmup.warmUp()
            ToastOverlayHost.init()
        }
        ClientLifecycleEvent.Starting.addPhaseOrdering(initPhase, Event.DEFAULT_PHASE)
    }


}
