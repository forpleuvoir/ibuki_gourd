package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.config.ClientModConfigHandler
import moe.forpleuvoir.ibukigourd.config.ServerModConfigHandler
import moe.forpleuvoir.ibukigourd.platform.INITS
import moe.forpleuvoir.ibukigourd.platform.PLATFORM
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.common.api.Initializable

object IbukiGourd {

    val logger = logger()

    const val MOD_ID: String = "ibukigourd"

    const val MOD_NAME: String = "IbukiGourd"

    val inits = listOf<Initializable>(
        ClientModConfigHandler,
        ServerModConfigHandler
    )

    fun init() {
        logger.info("$MOD_ID,platform:{},env:{}", PLATFORM.getPlatformName(), PLATFORM.getEnvironmentName())
        INITS.forEach { it.init() }
        inits.forEach { it.init() }
    }

}