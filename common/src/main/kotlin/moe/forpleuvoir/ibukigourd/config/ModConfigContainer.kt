package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.container.ConfigContainerImpl
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement

open class ModConfigContainer(key: String, autoScan: AutoScan = AutoScan.close) : ConfigContainerImpl(key, autoScan) {

    private val log = logger()

    override fun deserializationExceptionHandler(config: ConfigSerializable, serializeElement: SerializeElement, e: DeserializationException) {
        configManager()?.markSavable()
        log.error("${config.key}:${serializeElement} deserialization failed", e)
    }
}