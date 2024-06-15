package moe.forpleuvoir.ibukigourd.config.item.impl

import moe.forpleuvoir.ibukigourd.config.item.ConfigMarginValue
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.serialization.base.SerializeElement

class ConfigMargin(
	override val key: String,
    override val defaultValue: moe.forpleuvoir.ibukigourd.gui.base.Margin
) : ConfigBase<moe.forpleuvoir.ibukigourd.gui.base.Margin, ConfigMargin>(), ConfigMarginValue {

    override var configValue: moe.forpleuvoir.ibukigourd.gui.base.Margin = defaultValue

    override fun setValue(value: moe.forpleuvoir.ibukigourd.gui.base.Margin) {
		if (value notEquals this.configValue) {
			this.configValue = value
			this.onChange(this)
		}
	}

	override fun deserialization(serializeElement: SerializeElement) {
        configValue = moe.forpleuvoir.ibukigourd.gui.base.Margin.deserialization(serializeElement)
	}

	override fun serialization(): SerializeElement {
        return moe.forpleuvoir.ibukigourd.gui.base.Margin.serialization(configValue)
	}

}