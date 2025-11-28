package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.manager.ConfigManagerImpl
import moe.forpleuvoir.nebula.config.manager.component.localConfig
import moe.forpleuvoir.nebula.config.manager.components
import moe.forpleuvoir.nebula.config.persistence.jsonPersistence
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import java.nio.file.Path

abstract class ModConfigManager(val modId: String, key: String, autoScan: AutoScan = AutoScan.close) : ConfigManagerImpl(key, autoScan) {

    init {
        components {
            localConfig(configPath, jsonPersistence())
        }
    }

    abstract val configPath: Path

    private val log = logger()

    override fun deserializationExceptionHandler(config: ConfigSerializable, serializeElement: SerializeElement, e: DeserializationException) {
        markSavable()
        log.error("${config.key}:${serializeElement} deserialization failed", e)
    }
}