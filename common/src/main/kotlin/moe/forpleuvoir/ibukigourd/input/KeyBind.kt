package moe.forpleuvoir.ibukigourd.input

import com.google.common.collect.Lists
import moe.forpleuvoir.ibukigourd.IGLang
import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.input.KeyTriggerMode.*
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.exactMatch
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.common.api.Matchable
import moe.forpleuvoir.nebula.common.api.Notifiable
import moe.forpleuvoir.nebula.common.api.Resettable
import moe.forpleuvoir.nebula.serialization.Deserializable
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.extensions.serializeArray
import moe.forpleuvoir.nebula.serialization.extensions.serializeObject
import java.util.*
import java.util.function.Consumer


@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
class KeyBind(
    vararg keyCodes: KeyCode,
    private val defaultSetting: KeyBindSetting = KeyBindSetting(),
    action: KeyBind.() -> Unit = {}
) : Tickable, Resettable, Notifiable<KeyBind>, Matchable, Serializable, Deserializable {

    constructor(keyBind: KeyBind) : this(
        *keyBind.keys.toTypedArray(),
        defaultSetting = keyBind.setting,
        action = keyBind.action
    )

    companion object {

        private val log = logger(KeyBind::class)

    }

    val uuid: UUID = UUID.randomUUID()

    var name: Text = Literal(uuid.toString())

    private val defaultKeys: MutableList<KeyCode> = keyCodes.toMutableList()

    val setting: KeyBindSetting = KeyBindSetting().apply { copyFrom(defaultSetting) }

    val keys: MutableList<KeyCode> = ArrayList(defaultKeys)

    var action: KeyBind.() -> Unit = action

    /**
     * 当前按键是否被按下
     */
    var wasPress: Boolean = false
        private set

    /**
     * 按键被按下的时间，释放时会清空
     */
    private var tickCount: Long = 0

    fun name(name: Text): KeyBind {
        this.name = name
        return this
    }

    fun setKey(vararg keyCodes: KeyCode): Boolean {
        return if (!keys.exactMatch(keyCodes.toList())) {
            keys.clear()
            keys.addAll(keyCodes.toMutableList())
            onChange(this)
            true
        } else false
    }

    private fun List<KeyCode>.hasAll(elements: List<KeyCode>): Boolean {
        return this.map { it.code }.containsAll(elements.map { it.code })
    }

    fun onKeyPress(beforeKeyCode: List<KeyCode>, currentKeyCode: List<KeyCode>): NextAction {
        if (currentKeyCode.isEmpty() || !setting.environment.envMatch()) {
            wasPress = false
            return NextAction.Continue
        }
        val beforeMatched = if (setting.exactMatch) {
            keys.exactMatch(beforeKeyCode)
        } else {
            keys == beforeKeyCode || beforeKeyCode.hasAll(keys)
        }
        wasPress = if (setting.exactMatch) {
            keys.exactMatch(currentKeyCode)
        } else {
            keys == currentKeyCode || currentKeyCode.hasAll(keys)
        }
        if (wasPress && !beforeMatched) {
            return if (setting.triggerMode == OnPress || setting.triggerMode == Both) {
                action()
                setting.nextAction
            } else NextAction.Continue
        }
        return NextAction.Continue
    }

    fun onKeyRelease(beforeKeyCode: List<KeyCode>, currentKeyCode: List<KeyCode>): NextAction {
        if (beforeKeyCode.isEmpty() || !setting.environment.envMatch()) {
            wasPress = false
            return NextAction.Continue
        }
        val beforeMatched = if (setting.exactMatch) {
            keys.exactMatch(beforeKeyCode)
        } else {
            keys == beforeKeyCode || beforeKeyCode.hasAll(keys)
        }
        wasPress = if (setting.exactMatch) {
            if (beforeKeyCode.exactMatch(keys)) false else wasPress
        } else {
            if (beforeKeyCode.hasAll(keys)) false else wasPress
        }
        val currentMath = if (setting.exactMatch) {
            keys.exactMatch(currentKeyCode)
        } else {
            keys == currentKeyCode || currentKeyCode.hasAll(keys)
        }
        if (beforeMatched && !currentMath) {
            return if (setting.triggerMode == OnRelease || setting.triggerMode == Both) {
                action()
                setting.nextAction
            } else NextAction.Continue
        }
        return NextAction.Continue
    }

    override fun onTick() {
        if (wasPress) tickCount++
        if (wasPress)
            when (setting.triggerMode) {
                OnLongPress   -> {
                    if (tickCount == setting.longPressTime) action()
                }

                OnPressed     -> {
                    if (tickCount % setting.repeatTriggerInterval == 0L) action()
                }

                OnLongPressed -> {
                    val temp = tickCount - setting.longPressTime
                    if (temp >= 0 && tickCount % setting.repeatTriggerInterval == 0L) action()
                }

                else          -> Unit
            }
        if (!wasPress) tickCount = 0
    }

    val asTexts: List<Text>
        get() = keys.map { it.keyNameText }

    val asTranslatableKey: List<String>
        get() = keys.map { it.translationKey }

    val asText: Text
        get() {
            val texts = asTexts
            return if (texts.isEmpty()) {
                IGLang.notSpecified
            } else {
                Literal(asTexts.joinToString(" + ") { it.plainText })
            }
        }

    override fun matched(regex: Regex): Boolean {
        return regex.run {
            keys.any { it matched this } || setting matched this
        }
    }

    private val onChangedCallback: MutableList<Consumer<KeyBind>> = Lists.newArrayList()

    override fun onChange(value: KeyBind) {
        onChangedCallback.forEach { it.accept(value) }
    }

    override fun subscribe(callback: Consumer<KeyBind>) {
        onChangedCallback.add(callback)
    }

    override fun isDefault(): Boolean = defaultKeys.exactMatch(keys) && setting == defaultSetting

    override fun restDefault() {
        setting.copyFrom(defaultSetting)
        setKey(*defaultKeys.toTypedArray())
    }

    fun rest() {
        wasPress = false
        tickCount = 0
    }

    fun copyOf(target: KeyBind): Boolean {
        var valueChange = setting.copyFrom(target.setting)
        if (this.setKey(*target.keys.toTypedArray())) valueChange = true
        if (action != target.action) {
            action = target.action
            valueChange = true
        }
        return valueChange
    }

    override fun serialization(): SerializeElement {
        return serializeObject {
            "keys" to serializeArray(keys.map { KeyCode.serialization(it) })
            if (setting != defaultSetting) "setting" to setting
        }
    }

    override fun deserialization(serializeElement: SerializeElement) {
        val obj = serializeElement.asObject
        keys.clear()
        runCatching {
            obj["keys"]!!.asArray.forEach { keys.add(KeyCode.deserialization(it)) }
        }.onFailure {
            keys.addAll(defaultKeys.toSet())
        }
        runCatching {
            setting.deserialization(obj["setting"]!!)
        }.onFailure {
            setting.copyFrom(defaultSetting)
        }
        onChange(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyBind

        if (setting != other.setting) return false
        if (keys != other.keys) return false

        return true
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }

}