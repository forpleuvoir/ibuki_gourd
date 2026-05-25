package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.config.ClientModConfigHandler
import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import moe.forpleuvoir.ibukigourd.ui.skia.SkiaContext
import moe.forpleuvoir.ibukigourd.util.logger

object IbukiGourdClient {

    private val logger = logger()

    private val inits = listOf(
        ClientModConfigHandler
    )

    fun init() {
        ClientLifecycleEvent.Starting.register {
            SkiaContext.init()
        }
        inits.forEach { it.init() }
    }
}
