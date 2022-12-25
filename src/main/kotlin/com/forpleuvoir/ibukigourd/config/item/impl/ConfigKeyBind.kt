package com.forpleuvoir.ibukigourd.config.item.impl

import com.forpleuvoir.ibukigourd.config.item.ConfigKeyBindValue
import com.forpleuvoir.ibukigourd.input.KeyBind
import com.forpleuvoir.nebula.config.ConfigBase
import com.forpleuvoir.nebula.serialization.base.SerializeElement

class ConfigKeyBind(
	override val key: String,
	override val defaultValue: KeyBind
) : ConfigBase<KeyBind, ConfigKeyBind>(), ConfigKeyBindValue {
	override var configValue: KeyBind
		get() = TODO("Not yet implemented")
		set(value) {}

	override fun deserialization(serializeElement: SerializeElement) {
		TODO("Not yet implemented")
	}

	override fun serialization(): SerializeElement {
		TODO("Not yet implemented")
	}
}