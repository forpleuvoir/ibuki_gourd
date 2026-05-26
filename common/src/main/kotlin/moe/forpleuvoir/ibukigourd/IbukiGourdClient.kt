package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.api.ClientResourceReloaderListener
import moe.forpleuvoir.ibukigourd.config.ClientModConfigHandler
import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import moe.forpleuvoir.ibukigourd.ui.skia.SkiaContext
import moe.forpleuvoir.ibukigourd.ui.util.render.SkiaItemRenderHelper
import moe.forpleuvoir.ibukigourd.util.logger

object IbukiGourdClient {

    private val logger = logger()

    private val inits = listOf(
        ClientModConfigHandler
    )

    private val clientResourceReloaderListener = listOf<ClientResourceReloaderListener>(
        SkiaItemRenderHelper
    )

    fun addClientResourceReloaderListener(listener: (ClientResourceReloaderListener) -> Unit) {
        clientResourceReloaderListener.forEach(listener)
    }


    fun init() {
        ClientLifecycleEvent.Starting.register {
            SkiaContext.init()
        }
        inits.forEach { it.init() }


    }


}
