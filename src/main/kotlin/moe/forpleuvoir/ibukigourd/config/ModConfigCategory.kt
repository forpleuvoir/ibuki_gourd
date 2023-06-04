package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.impl.ConfigCategoryImpl
import moe.forpleuvoir.nebula.serialization.base.SerializeElement

open class ModConfigCategory(key: String) : ConfigCategoryImpl(key) {

	private val log = logger()

	override fun deserializationExceptionHandler(configSerializable: ConfigSerializable, serializeElement: SerializeElement, e: Exception) {
		needSave = true
		log.error("${configSerializable.key}:${serializeElement} deserialization failed", e)
	}
}