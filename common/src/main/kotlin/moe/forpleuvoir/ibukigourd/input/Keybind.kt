package moe.forpleuvoir.ibukigourd.input

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.input.KeyTriggerTiming.*
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.MutableText
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.util.exactMatch
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.common.api.Matchable
import moe.forpleuvoir.nebula.common.api.Observable
import moe.forpleuvoir.nebula.common.api.Resettable
import moe.forpleuvoir.nebula.common.util.checkType
import moe.forpleuvoir.nebula.common.util.requireKey
import moe.forpleuvoir.nebula.common.util.requireKeysOrNull
import moe.forpleuvoir.nebula.common.util.requireType
import moe.forpleuvoir.nebula.serialization.DeserializationException
import moe.forpleuvoir.nebula.serialization.Serde
import moe.forpleuvoir.nebula.serialization.base.SerializeArray
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializeObject
import moe.forpleuvoir.nebula.serialization.base.builder.build
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.list
import java.util.concurrent.CopyOnWriteArrayList


private fun normalizeKeys(keys: Iterable<KeyCode>): List<KeyCode> = keys.distinct()


@Suppress("MemberVisibilityCanBePrivate")
class Keybind(
    vararg keyCodes: KeyCode,
    private val defaultSetting: KeybindSetting = KeybindSetting(),
    action: Keybind.() -> Unit = {}
) : Tickable, Resettable, Observable<Keybind>, Matchable<Regex>, Serde {

    constructor(keyBind: Keybind) : this(
        *keyBind.keys.toTypedArray(),
        defaultSetting = keyBind.setting,
        action = keyBind.action
    )

    companion object {

        private val log = logger(Keybind::class)

        private val keysCodec = Codec.list(KeyCode)

    }

    private var _name: Text? = null

    var name: Text
        set(value) {
            _name = value
        }
        get() = _name ?: Text.literal(keys.joinToString(" + ") { it.keyName })

    private val defaultKeys: List<KeyCode> = normalizeKeys(keyCodes.asIterable())

    var setting: KeybindSetting = defaultSetting
        private set

    val keys: List<KeyCode>
        field: MutableList<KeyCode> = CopyOnWriteArrayList(defaultKeys)

    var action: Keybind.() -> Unit = action

    /**
     * 当前按键是否被按下
     */
    var wasPress: Boolean = false
        private set

    /**
     * 按键被按下的时间，释放时会清空
     */
    private var tickCount: Long = 0

    fun setKey(vararg keyCodes: KeyCode): Boolean {
        val normalized = normalizeKeys(keyCodes.asIterable())
        return if (keys != normalized) {
            keys.clear()
            keys.addAll(normalized)
            resetState()
            notifyChange(this)
            true
        } else false
    }

    private fun List<KeyCode>.matchKeys(elements: List<KeyCode>): Boolean {
        return elements.all { this.contains(it) }
    }

    fun onKeyPress(beforeKeyCode: List<KeyCode>, currentKeyCode: List<KeyCode>): Boolean {
        if (keys.isEmpty()) return true
        if (currentKeyCode.isEmpty() || !setting.env.envMatch()) {
            wasPress = false
            return true
        }
        val beforeMatched = if (setting.strict) {
            keys == beforeKeyCode
        } else {
            beforeKeyCode.isNotEmpty() && (keys == beforeKeyCode || beforeKeyCode.matchKeys(keys))
        }
        wasPress = if (setting.strict) {
            keys == currentKeyCode
        } else {
            keys == currentKeyCode || currentKeyCode.matchKeys(keys)
        }
        return !(wasPress && !beforeMatched) || if (setting.trigger == Press || setting.trigger == PressAndRelease) {
            action()
            setting.passthrough
        } else true
    }

    fun onKeyRelease(beforeKeyCode: List<KeyCode>, currentKeyCode: List<KeyCode>): Boolean {
        if (keys.isEmpty()) return true
        if (beforeKeyCode.isEmpty() || !setting.env.envMatch()) {
            wasPress = false
            return true
        }
        val beforeMatched = if (setting.strict) {
            keys == beforeKeyCode
        } else {
            keys == beforeKeyCode || beforeKeyCode.matchKeys(keys)
        }
        val currentMatched = if (setting.strict) {
            keys == currentKeyCode
        } else {
            keys == currentKeyCode || currentKeyCode.matchKeys(keys)
        }
        wasPress = currentMatched
        return !(beforeMatched && !currentMatched) || if (setting.trigger == Release || setting.trigger == PressAndRelease) {
            action()
            setting.passthrough
        } else true
    }

    override fun onTick() {
        if (wasPress) {
            tickCount++
            when (setting.trigger) {
                LongPress        -> {
                    if (tickCount == setting.longPressThreshold.toLong()) action()
                }

                WhilePressed     -> {
                    if (tickCount % setting.repeatInterval == 0L) action()
                }

                WhileLongPressed -> {
                    val elapsed = tickCount - setting.longPressThreshold
                    if (elapsed >= 0 && elapsed % setting.repeatInterval == 0L) action()
                }

                else             -> Unit
            }
        } else tickCount = 0
    }

    val asTexts: List<MutableText>
        get() = keys.map { it.keyNameText }

    val asTranslatableKey: List<String>
        get() = keys.map { it.translationKey }

    val asText: MutableText
        get() {
            val texts = asTexts
            return if (texts.isEmpty()) {
                IGLang.Misc.notSpecified
            } else {
                Literal(texts.joinToString(" + ") { it.plainText })
            }
        }

    override fun matched(target: Regex): Boolean {
        return target.run {
            keys.any { it matched this } || setting matched this || containsMatchIn(name.plainText)
        }
    }

    private val observers: MutableList<(Keybind) -> Unit> = CopyOnWriteArrayList()

    override fun notifyChange(value: Keybind) {
        observers.forEach { it(value) }
    }

    override fun observe(callback: (Keybind) -> Unit): Observable.Disposable {
        observers.add(callback)
        return { observers.remove(callback) }
    }

    override fun isDefault(): Boolean = defaultKeys.exactMatch(keys) && setting == defaultSetting

    override fun resetDefault() {
        if (isDefault()) return

        setting = defaultSetting
        keys.clear()
        keys.addAll(defaultKeys)
        resetState()
        notifyChange(this)
    }

    fun resetState() {
        wasPress = false
        tickCount = 0
    }

    private fun setFrom(keys: List<KeyCode>, setting: KeybindSetting): Boolean {
        var valueChange = false
        if (this.setting != setting) {
            this.setting = setting
            valueChange = true
        }
        val normalized = normalizeKeys(keys.asIterable())
        if (this.keys != normalized) {
            this.keys.clear()
            this.keys.addAll(normalized)
            valueChange = true
        }
        if (valueChange) resetState()
        return valueChange
    }

    fun setFrom(target: Keybind, action: Boolean = true): Boolean {
        var valueChange = setFrom(target.keys, target.setting)
        if (action && this.action != target.action) {
            this.action = target.action
            valueChange = true
        }
        if (valueChange) notifyChange(this)
        return valueChange
    }

    fun setFrom(setting: KeybindSetting): Boolean {
        var valueChange = false
        if (this.setting != setting) {
            this.setting = setting
            valueChange = true
        }
        if (valueChange) {
            resetState()
            notifyChange(this)
        }
        return valueChange
    }

    override fun serialization(): SerializeElement = SerializeObject.build {
        set("keys", keys, keysCodec)
        if (setting != defaultSetting) "setting"(KeybindSetting.serialization(setting))
    }

    override fun deserialization(data: SerializeElement) {
        DeserializationException.runCatching {
            data.checkType<SerializeObject, Unit> { obj ->
                val keys = keysCodec.deserialization(
                    obj.requireKey("keys").requireType<SerializeArray>()
                ).onFailure {
                    log.warn("Failed to deserialize Keybind keys, will keep current value", it)
                }.getOrDefault(this.keys).toList()


                val setting = obj.requireKeysOrNull("setting")?.let { obj ->
                    KeybindSetting.deserialization(
                        obj.requireKey("setting").requireType<SerializeObject>("Keybind setting decode,")
                    ).onFailure {
                        log.warn("Failed to deserialize Keybind setting, will keep current value", it)
                    }.getOrDefault(this.setting)
                } ?: this.setting

                if (setFrom(keys, setting)) {
                    notifyChange(this)
                }
            }
        }.onFailure {
            log.warn("Failed to deserialize Keybind", it)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Keybind

        if (setting != other.setting) return false
        if (keys != other.keys) return false

        return true
    }

    override fun toString(): String {
        return "Keybind(name=${name.plainText}, keys=${keys.joinToString(" + ") { it.keyName }}, setting=$setting)"
    }

    override fun hashCode(): Int {
        var result = wasPress.hashCode()
        result = 31 * result + tickCount.hashCode()
        result = 31 * result + defaultSetting.hashCode()
        result = 31 * result + (_name?.hashCode() ?: 0)
        result = 31 * result + defaultKeys.hashCode()
        result = 31 * result + setting.hashCode()
        result = 31 * result + keys.hashCode()
        result = 31 * result + action.hashCode()
        result = 31 * result + observers.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + keys.hashCode()
        result = 31 * result + asTexts.hashCode()
        result = 31 * result + asTranslatableKey.hashCode()
        result = 31 * result + asText.hashCode()
        return result
    }


}