package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.impl.LocalConfigManager
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import net.fabricmc.loader.api.metadata.ModMetadata

abstract class ModConfigManager(protected val modMetadata: ModMetadata, key: String) : LocalConfigManager(key) {

	private val log = logger()

	override fun deserializationExceptionHandler(configSerializable: ConfigSerializable, serializeElement: SerializeElement, e: Exception) {
		needSave = true
		log.error("${configSerializable.key}:${serializeElement} deserialization failed", e)
	}
}