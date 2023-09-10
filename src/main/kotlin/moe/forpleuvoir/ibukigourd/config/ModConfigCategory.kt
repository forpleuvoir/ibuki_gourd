package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.config.ConfigSerializable
import moe.forpleuvoir.nebula.config.category.ConfigCategoryImpl
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.base.SerializeElement

open class ModConfigCategory(key: String) : ConfigCategoryImpl(key) {

	private val log = logger()

	override fun deserializationExceptionHandler(configSerializable: ConfigSerializable, serializeElement: SerializeElement, e: DeserializationException) {
		needSave = true
		log.error("${configSerializable.key}:${serializeElement} deserialization failed", e)
	}
}