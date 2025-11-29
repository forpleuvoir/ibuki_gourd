package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.event.IbukiGourdEventManager
import moe.forpleuvoir.ibukigourd.event.events.IbukigourdInitializerEvent
import moe.forpleuvoir.ibukigourd.platform.PLATFORM
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.event.EventBus

object IbukiGourd {

    val logger = logger()

    const val MOD_ID: String = "ibukigourd"

    const val MOD_NAME: String = "IbukiGourd"

    fun init() {
        logger.info("Ibukigourd,platform:{},env:{}", PLATFORM.getPlatformName(), PLATFORM.getEnvironmentName())
        IbukiGourdEventManager.init()
        EventBus.broadcast(IbukigourdInitializerEvent)
    }

}