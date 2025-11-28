package moe.forpleuvoir.ibukigourd.platform

import moe.forpleuvoir.ibukigourd.platform.services.PlatformHelper
import moe.forpleuvoir.ibukigourd.util.logger
import java.util.*

object Services {

    private val logger = logger()

    val PLATFORM = load(PlatformHelper::class.java)

    fun <T> load(clazz: Class<T>): T {
        val loadedService = ServiceLoader.load(clazz)
            .findFirst()
            .orElseThrow {
                IllegalStateException("Failed to load service for ${clazz.name}")
            }
        logger.debug("loaded {} for service {}", loadedService, clazz)
        return loadedService
    }

}

val PLATFORM get() = Services.PLATFORM

val isDevEnv: Boolean by lazy { PLATFORM.isDevEnvironment() }

