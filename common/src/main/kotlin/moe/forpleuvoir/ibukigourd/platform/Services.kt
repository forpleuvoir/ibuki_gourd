package moe.forpleuvoir.ibukigourd.platform

import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager
import moe.forpleuvoir.ibukigourd.config.ServerModConfigManager
import moe.forpleuvoir.ibukigourd.platform.services.ModInitialization
import moe.forpleuvoir.ibukigourd.platform.services.PlatformHelper
import moe.forpleuvoir.ibukigourd.util.logger
import java.util.*

@Suppress("LoggingSimilarMessage")
object Services {

    private val logger = logger()

    val PLATFORM = load(PlatformHelper::class.java)

    val INITS = loadAll(ModInitialization::class.java)

    fun <T> load(clazz: Class<T>): T {
        val loadedService = ServiceLoader.load(clazz)
            .findFirst()
            .orElseThrow {
                IllegalStateException("Failed to load service for ${clazz.name}")
            }
        logger.debug("loaded {} for service {}", loadedService, clazz)
        return loadedService
    }

    fun <T> loadAll(clazz: Class<T>): List<T> {
        val loadedService = ServiceLoader.load(clazz)
            .toList()
        loadedService.forEach { service ->
            logger.debug("loaded {} for service {}", service, clazz)
        }
        return loadedService
    }

}

internal inline val PLATFORM get() = Services.PLATFORM

internal inline val INITS get() = Services.INITS

val isDevEnv: Boolean by lazy { PLATFORM.isDevEnvironment() }

