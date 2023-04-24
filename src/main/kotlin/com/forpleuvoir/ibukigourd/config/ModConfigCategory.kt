package com.forpleuvoir.ibukigourd.config

import com.forpleuvoir.ibukigourd.util.logger
import com.forpleuvoir.nebula.config.ConfigSerializable
import com.forpleuvoir.nebula.config.impl.ConfigCategoryImpl
import com.forpleuvoir.nebula.serialization.base.SerializeElement

open class ModConfigCategory(key: String) : ConfigCategoryImpl(key) {

	private val log = logger()

	override fun deserializationExceptionHandler(configSerializable: ConfigSerializable, serializeElement: SerializeElement, e: Exception) {
		needSave = true
		log.error("${configSerializable.key}:${serializeElement} deserialization failed", e)
	}
}