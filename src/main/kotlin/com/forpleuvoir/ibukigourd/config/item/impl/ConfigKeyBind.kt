package com.forpleuvoir.ibukigourd.config.item.impl

import com.forpleuvoir.ibukigourd.config.item.ConfigKeyBindValue
import com.forpleuvoir.ibukigourd.input.InputHandler
import com.forpleuvoir.ibukigourd.input.KeyBind
import com.forpleuvoir.nebula.config.ConfigBase
import com.forpleuvoir.nebula.serialization.base.SerializeElement

class ConfigKeyBind(
	override val key: String,
	override val defaultValue: KeyBind
) : ConfigBase<KeyBind, ConfigKeyBind>(), ConfigKeyBindValue {

	override var configValue: KeyBind = KeyBind(defaultValue)

	override fun init() {
		super.init()
		InputHandler.register(configValue)
	}

	override fun setValue(value: KeyBind) {
		configValue.copyOf(value)
	}

	override fun deserialization(serializeElement: SerializeElement) {
		configValue.deserialization(serializeElement)
	}

	override fun serialization(): SerializeElement {
		return configValue.serialization()
	}

}