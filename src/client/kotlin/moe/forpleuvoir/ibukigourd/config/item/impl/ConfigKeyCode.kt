package moe.forpleuvoir.ibukigourd.config.item.impl

import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement

class ConfigKeyCode(
    override val key: String,
    override val defaultValue: KeyCode
) : ConfigBase<KeyCode, ConfigKeyCode>() {

    override var configValue: KeyCode = defaultValue

    override fun serialization(): SerializeElement = KeyCode.serialization(configValue)

    override fun deserialization(serializeElement: SerializeElement) {
        configValue = KeyCode.deserialization(serializeElement)
        onChange(this)
    }

    override fun matched(regex: Regex): Boolean {
        return super.matched(regex) || configValue matched regex
    }

}

fun ConfigContainer.keyCode(key: String, defaultValue: KeyCode) = addConfig(ConfigKeyCode(key, defaultValue))