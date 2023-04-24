package com.forpleuvoir.ibukigourd.config.item.impl

import com.forpleuvoir.ibukigourd.config.item.ConfigMarginValue
import com.forpleuvoir.ibukigourd.gui.base.Margin
import com.forpleuvoir.nebula.config.ConfigBase
import com.forpleuvoir.nebula.serialization.base.SerializeElement

class ConfigMargin(
	override val key: String,
	override val defaultValue: Margin
) : ConfigBase<Margin, ConfigMargin>(), ConfigMarginValue {

	override var configValue: Margin = defaultValue

	override fun setValue(value: Margin) {
		if (value notEquals this.configValue) {
			this.configValue = value
			this.onChange(this)
		}
	}

	override fun deserialization(serializeElement: SerializeElement) {
		configValue = Margin.deserialization(serializeElement)
	}

	override fun serialization(): SerializeElement {
		return Margin.serialization(configValue)
	}

}