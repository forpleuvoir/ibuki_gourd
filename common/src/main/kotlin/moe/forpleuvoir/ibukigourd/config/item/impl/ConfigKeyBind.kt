package moe.forpleuvoir.ibukigourd.config.item.impl

import moe.forpleuvoir.ibukigourd.config.item.ConfigKeyBindValue
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.KeyBind
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.appendLiteral
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement

class ConfigKeyBind(
    override val key: String,
    override val defaultValue: KeyBind
) : ConfigBase<KeyBind, ConfigKeyBind>(), ConfigKeyBindValue {

    override var configValue: KeyBind = KeyBind(defaultValue)

    override fun init() {
        super.init()
        InputHandler.register(configValue)
        configValue.name((this.parentContainer?.translateText?.appendLiteral("->") ?: Literal()).append(this.translateText))
    }

    override fun setValue(value: KeyBind) {
        if (configValue.copyOf(value)) {
            onChange(this)
        }
    }

    override fun deserialization(serializeElement: SerializeElement) {
        configValue.deserialization(serializeElement)
        onChange(this)
    }

    override fun serialization(): SerializeElement {
        return configValue.serialization()
    }

    override fun matched(regex: Regex): Boolean {
        return super.matched(regex) || configValue.matched(regex)
    }

}

fun ConfigContainer.keyBind(key: String, defaultValue: KeyBind) = addConfig(ConfigKeyBind(key, defaultValue))