package moe.forpleuvoir.ibukigourd.config

import kotlinx.coroutines.awaitAll
import moe.forpleuvoir.nebula.common.util.ioAsync
import kotlin.time.Duration

interface ModConfigHandler<T : ModConfigManager> {

    val managers: Iterable<T>

    fun register(manager: T)

    suspend fun save() {
        managers.forEach { it.save() }
    }

    fun asyncSave() = ioAsync {
        managers.map { it.asyncSave() }
            .awaitAll()
            .maxOrNull() ?: Duration.ZERO
    }


    suspend fun load() {
        managers.forEach { it.load() }
    }

    fun asyncLoad() = ioAsync {
        managers.map { it.asyncLoad() }
            .awaitAll()
            .maxOrNull() ?: Duration.ZERO
    }

}