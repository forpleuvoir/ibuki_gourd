package moe.forpleuvoir.ibukigourd.config.item.impl

import moe.forpleuvoir.ibukigourd.config.item.ConfigKeyBindBooleanValue
import moe.forpleuvoir.ibukigourd.config.item.KeyBindWithBoolean
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.KeyBind
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement

class ConfigKeyBindBoolean(
    override val key: String,
    override val defaultValue: KeyBindWithBoolean
) : ConfigBase<KeyBindWithBoolean, ConfigKeyBindBoolean>(), ConfigKeyBindBooleanValue {

    private val action: KeyBind.() -> Unit = {
        configValue.value = !configValue.value
    }

    override var configValue: KeyBindWithBoolean = KeyBindWithBoolean(KeyBind(defaultValue.keyBind.apply {
        this.action = this@ConfigKeyBindBoolean.action
    }), defaultValue.value)

    override fun init() {
        super.init()
        InputHandler.register(configValue.keyBind)
    }

    override fun setValue(value: KeyBindWithBoolean) {
        value.keyBind.action = action
        configValue.keyBind.copyOf(value.keyBind)
        configValue.value = value.value
    }

    override fun deserialization(serializeElement: SerializeElement) {
        configValue.deserialization(serializeElement)
    }

    override fun serialization(): SerializeElement {
        return configValue.serialization()
    }

    override fun matched(regex: Regex): Boolean {
        return super matched regex || configValue matched regex
    }

}

fun ConfigContainer.keyBindBoolean(key: String, defaultValue: KeyBindWithBoolean) = addConfig(ConfigKeyBindBoolean(key, defaultValue))

fun ConfigContainer.keyBindBoolean(key: String, keyBind: KeyBind, value: Boolean) = addConfig(ConfigKeyBindBoolean(key, KeyBindWithBoolean(keyBind, value)))