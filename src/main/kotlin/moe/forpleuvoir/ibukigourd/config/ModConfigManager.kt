package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.manager.ConfigManagerImpl
import moe.forpleuvoir.nebula.config.manager.component.localConfig
import moe.forpleuvoir.nebula.config.manager.components
import moe.forpleuvoir.nebula.config.persistence.jsonPersistence
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import net.fabricmc.loader.api.metadata.ModMetadata
import java.nio.file.Path

abstract class ModConfigManager(val modMetadata: ModMetadata, key: String) : ConfigManagerImpl(key) {

    init {
        components {
            localConfig(configPath, jsonPersistence())
        }
    }

    abstract val configPath: Path

    private val log = logger()

    fun step() {

    }

    override fun deserializationExceptionHandler(config: ConfigSerializable, serializeElement: SerializeElement, e: DeserializationException) {
        markSavable()
        log.error("${config.key}:${serializeElement} deserialization failed", e)
    }
}