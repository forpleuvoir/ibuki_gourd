package moe.forpleuvoir.ibukigourd

import moe.forpleuvoir.ibukigourd.config.ClientModConfigHandler
import moe.forpleuvoir.ibukigourd.util.logger

object IbukiGourdClient {

    private val logger = logger()

    private val inits = listOf(
        ClientModConfigHandler
    )

    fun init() {
        inits.forEach { it.init() }
    }
}
