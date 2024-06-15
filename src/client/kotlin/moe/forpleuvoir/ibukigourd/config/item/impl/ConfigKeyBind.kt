package moe.forpleuvoir.ibukigourd.config.item.impl

import moe.forpleuvoir.ibukigourd.config.item.ConfigKeyBindValue
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.KeyBind
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.serialization.base.SerializeElement

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