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

abstract class ModConfigManager(protected val modMetadata: ModMetadata, key: String) : ConfigManagerImpl(key) {

    init {
        components {
            localConfig(configPath, jsonPersistence())
        }
    }

    abstract val configPath: Path

    private val log = logger()

    override fun deserializationExceptionHandler(configSerializable: ConfigSerializable, serializeElement: SerializeElement, e: DeserializationException) {
        needSave = true
        log.error("${configSerializable.key}:${serializeElement} deserialization failed", e)
    }
}