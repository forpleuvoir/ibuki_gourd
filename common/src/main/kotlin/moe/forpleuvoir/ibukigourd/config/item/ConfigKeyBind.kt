package moe.forpleuvoir.ibukigourd.config.item

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.config.translateTextWithParent
import moe.forpleuvoir.ibukigourd.input.InputHandler
import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.Keybind
import moe.forpleuvoir.ibukigourd.input.KeybindSetting
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.appendLTRArrow
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigRowWrapper
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.ibukigourd.ui.toast.ToastHandler
import moe.forpleuvoir.ibukigourd.ui.toast.ToastStrategy
import moe.forpleuvoir.nebula.common.api.Matchable
import moe.forpleuvoir.nebula.common.util.checkType
import moe.forpleuvoir.nebula.common.util.requireKey
import moe.forpleuvoir.nebula.common.util.requireType
import moe.forpleuvoir.nebula.config.Config
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.config
import moe.forpleuvoir.nebula.config.pathWithRoot
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.Serde
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.base.builder.build
import moe.forpleuvoir.nebula.serialization.codec.Codec

class ToggleKeybind(
    val keybind: Keybind,
    var enabled: Boolean
) : Serde, Matchable<Regex> {

    override fun serialization(): SerializeElement = SerializeObject.build {
        "keybind" to keybind.serialization()
        "enabled" to enabled
    }

    override fun deserialization(data: SerializeElement) = DeserializationException.runCatching {
        data.checkType<SerializeObject, Unit> { obj ->
            keybind.deserialization(obj.requireKey("keybind").requireType<SerializeObject>())
            enabled = Codec.boolean.deserialization(obj.requireKey("enabled").requireType<SerializePrimitive>()).getOrThrow()
        }
    }.getOrThrow()

    override fun matched(target: Regex): Boolean =
        keybind matched target || target.containsMatchIn(enabled.toString())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ToggleKeybind

        if (enabled != other.enabled) return false
        if (keybind != other.keybind) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + keybind.hashCode()
        return result
    }


}

class ConfigKeybind(
    name: String,
    defaultValue: Keybind
) : Config<Keybind>(name, Keybind(defaultValue)) {

    override fun init() {
        super.init()
        InputHandler.register(getValue())
        getValue().name = (this.parent?.translateText?.appendLTRArrow() ?: Literal()).append(this.translateText)
        getValue().observe {
            this.notifyChange()
        }
    }

    override var configValue: Keybind = Keybind(this.defaultValue)

    override fun resetDefault() {
        if (isDefault()) return
        setValue(defaultValue)
    }

    override fun setValue(value: Keybind) {
        getValue().setFrom(value, false)
    }

    override fun deserialization(data: SerializeElement) {
        getValue().deserialization(data)
    }

    override fun serialization(): SerializeElement {
        return getValue().serialization()
    }

}

context(group: ConfigGroup)
fun configKeybind(name: String, defaultValue: Keybind) = group.addConfig(ConfigKeybind(name, defaultValue))

context(group: ConfigGroup)
fun configKeybind(name: String, vararg defaultKeys: KeyCode, setting: KeybindSetting = KeybindSetting(), action: Keybind.() -> Unit) =
    group.addConfig(ConfigKeybind(name, Keybind(*defaultKeys, defaultSetting = setting, action = action)))


class ConfigToggleKeybind(
    name: String,
    defaultEnabled: Boolean,
    defaultKeybind: Keybind,
    private val onSwitch: ConfigToggleKeybind.() -> Unit
) : Config<ToggleKeybind>(name, ToggleKeybind(defaultKeybind, defaultEnabled)) {

    override fun init() {
        super.init()
        getValue().keybind.apply {
            action = {
                toggle()
                onSwitch()
            }
            InputHandler.register(this)
            name = (this@ConfigToggleKeybind.parent?.translateText?.appendLTRArrow() ?: Literal()).append(translateText)
            observe {
                this@ConfigToggleKeybind.notifyChange()
            }
        }
    }

    override var configValue: ToggleKeybind = ToggleKeybind(Keybind(this.defaultValue.keybind), this.defaultValue.enabled)

    override fun resetDefault() {
        if (isDefault()) return
        setValue(defaultValue)
    }

    inline var enabled: Boolean
        get() = getValue().enabled
        set(value) {
            if (value != enabled) toggle()
        }

    inline val keybind: Keybind get() = getValue().keybind

    fun toggle() {
        getValue().enabled = !enabled
        notifyChange()
    }

    override fun setValue(value: ToggleKeybind) {
        if (value == getValue()) return
        var needNotify = false
        if (value.enabled != getValue().enabled) {
            getValue().enabled = value.enabled
            needNotify = true
        }
        needNotify = getValue().keybind.setFrom(value.keybind, false) == false && needNotify
        if (needNotify) {
            notifyChange()
        }
    }

    override fun deserialization(data: SerializeElement) {
        getValue().deserialization(data)
    }

    override fun serialization(): SerializeElement {
        return getValue().serialization()
    }

}

context(group: ConfigGroup)
fun configToggleKeybind(
    name: String,
    defaultEnabled: Boolean,
    defaultValue: Keybind,
    onSwitch: ConfigToggleKeybind.() -> Unit = {
        val config = this
        ToastHandler.showContent(strategy = ToastStrategy.Tagged.Refresh("toggle_keybind:${pathWithRoot}")) {
            var enabled by remember { mutableStateOf(config.enabled) }
            val interval = ConfigRowWrapper.valuePollInterval
            LaunchedEffect(Unit) {
                while (isActive) {
                    enabled = config.enabled
                    delay(interval)
                }
            }
            Text(translateTextWithParent(1, " → ").append(" : ").append(IGLang.Misc.coloredSwitch(enabled)))
        }
    }
) =
    group.addConfig(ConfigToggleKeybind(name, defaultEnabled, defaultValue, onSwitch))

context(group: ConfigGroup)
fun configToggleKeybind(
    name: String,
    defaultEnabled: Boolean,
    vararg defaultKeys: KeyCode,
    setting: KeybindSetting = KeybindSetting(),
    onSwitch: ConfigToggleKeybind.() -> Unit = {
        val config = this
        ToastHandler.showContent(strategy = ToastStrategy.Tagged.Refresh("toggle_keybind:${pathWithRoot}")) {
            var enabled by remember { mutableStateOf(config.enabled) }
            val interval = ConfigRowWrapper.valuePollInterval
            LaunchedEffect(Unit) {
                while (isActive) {
                    enabled = config.enabled
                    delay(interval)
                }
            }
            Text(translateTextWithParent(1, " → ").append(" : ").append(IGLang.Misc.coloredSwitch(enabled)))
        }
    }
) =
    group.addConfig(ConfigToggleKeybind(name, defaultEnabled, Keybind(*defaultKeys, defaultSetting = setting), onSwitch))


context(group: ConfigGroup)
fun configKeyCode(name: String, defaultValue: KeyCode) =
    config(name, defaultValue, KeyCode)
