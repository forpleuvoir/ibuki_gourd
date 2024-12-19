package moe.forpleuvoir.ibukigourd.config.item.impl

import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.config.item.ConfigKeyBindBooleanValue
import moe.forpleuvoir.ibukigourd.config.item.KeyBindWithBoolean
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.KeyBind
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.nebula.common.color.Colors
import moe.forpleuvoir.nebula.common.util.primitive.pick
import moe.forpleuvoir.nebula.config.ConfigBase
import moe.forpleuvoir.nebula.config.container.ConfigContainer
import moe.forpleuvoir.nebula.serialization.base.SerializeElement

class ConfigKeyBindBoolean(
    override val key: String,
    override val defaultValue: KeyBindWithBoolean,
    onSwitch: ConfigKeyBindBoolean.(Boolean) -> Unit
) : ConfigBase<KeyBindWithBoolean, ConfigKeyBindBoolean>(), ConfigKeyBindBooleanValue {

    private val action: KeyBind.() -> Unit = {
        configValue.value = !configValue.value
        this@ConfigKeyBindBoolean.onSwitch(configValue.value)
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

    override fun KeyBindWithBoolean.isEquals(other: KeyBindWithBoolean): Boolean {
        return this.value == other.value && this.keyBind == other.keyBind
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

fun ConfigContainer.keyBindBoolean(
    key: String,
    defaultValue: KeyBindWithBoolean,
    onSwitch: ConfigKeyBindBoolean.(Boolean) -> Unit = {
        Toast.showToast(
            text =
                (this.parentContainer?.translateText ?: Literal())
                    .appendLiteral("->")
                    .append(this.translateText).append(Literal(" : "))
                    .append(
                        it.pick(IGLang.switchOn, IGLang.switchOff).withColor(it.pick(Colors.LIMEGREEN, Colors.RED))
                    )
        )
    }
) = addConfig(ConfigKeyBindBoolean(key, defaultValue, onSwitch))

fun ConfigContainer.keyBindBoolean(
    key: String,
    value: Boolean,
    keyBind: KeyBind = KeyBind(),
    onSwitch: ConfigKeyBindBoolean.(Boolean) -> Unit = {
        Toast.showToast(
            text = (this.parentContainer?.translateText ?: Literal())
                .appendLiteral("->")
                .append(this.translateText).append(Literal(" : "))
                .append(
                    it.pick(IGLang.switchOn, IGLang.switchOff).withColor(it.pick(Colors.LIMEGREEN, Colors.RED))
                )
        )
    }
) = addConfig(ConfigKeyBindBoolean(key, KeyBindWithBoolean(keyBind, value), onSwitch))