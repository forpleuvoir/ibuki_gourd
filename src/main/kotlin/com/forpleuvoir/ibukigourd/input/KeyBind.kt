package com.forpleuvoir.ibukigourd.input

import com.forpleuvoir.ibukigourd.api.Tickable
import com.forpleuvoir.ibukigourd.input.KeyTriggerMode.*
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.ibukigourd.util.exactMatch
import com.forpleuvoir.ibukigourd.util.logger
import com.forpleuvoir.ibukigourd.util.text.Text
import com.forpleuvoir.ibukigourd.util.text.literal
import com.forpleuvoir.nebula.common.api.Matchable
import com.forpleuvoir.nebula.common.api.Notifiable
import com.forpleuvoir.nebula.common.api.Resettable
import com.forpleuvoir.nebula.serialization.Deserializable
import com.forpleuvoir.nebula.serialization.Serializable
import com.forpleuvoir.nebula.serialization.base.SerializeElement
import com.forpleuvoir.nebula.serialization.extensions.serializeArray
import com.forpleuvoir.nebula.serialization.extensions.serializeObject
import com.google.common.collect.Lists
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.util.InputUtil
import java.util.*
import java.util.function.Consumer


@Suppress("MemberVisibilityCanBePrivate")
@Environment(EnvType.CLIENT)
class KeyBind(
	vararg keyCodes: KeyCode,
	private val defaultSetting: KeyBindSetting = keyBindSetting(),
	action: KeyBind.() -> Unit = {}
) : Tickable, Resettable, Notifiable<KeyBind>, Matchable, Serializable, Deserializable {

	private val log = logger()

	val uuid: UUID = UUID.randomUUID()

	private val defaultKeys: MutableList<KeyCode> = keyCodes.toMutableList()

	val setting: KeyBindSetting = keyBindSetting().apply { copyOf(defaultSetting) }

	val keys: MutableList<KeyCode> = ArrayList(defaultKeys)

	constructor(keyBind: KeyBind) : this(
		*keyBind.keys.toTypedArray(),
		defaultSetting = keyBind.setting,
		action = keyBind.action
	)

	var action: KeyBind.() -> Unit = action
		private set

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
			keys == beforeKeyCode || beforeKeyCode.hasAll(beforeKeyCode)
		}
		wasPress = if (setting.exactMatch) {
			keys.exactMatch(currentKeyCode)
		} else {
			keys == currentKeyCode || currentKeyCode.hasAll(currentKeyCode)
		}
		if (wasPress && !beforeMatched) {
			return if (setting.triggerMode == OnPress || setting.triggerMode == BOTH) {
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
			keys == beforeKeyCode || beforeKeyCode.hasAll(beforeKeyCode)
		}
		val currentMath = if (setting.exactMatch) {
			keys.exactMatch(currentKeyCode)
		} else {
			keys == currentKeyCode || currentKeyCode.hasAll(currentKeyCode)
		}
		if (beforeMatched && !currentMath) {
			return if (setting.triggerMode == OnRelease || setting.triggerMode == BOTH) {
				action()
				setting.nextAction
			} else NextAction.Continue
		}
		return NextAction.Continue
	}

	override fun tick() {
		if (wasPress) tickCount++
		if (wasPress)
			when (setting.triggerMode) {
				OnLongPress -> {
					if (tickCount == setting.longPressTime) action()
				}

				OnPressed -> {
					if (tickCount % setting.triggerPeriod == 0L) action()
				}

				OnLongPressed -> {
					val temp = tickCount - setting.longPressTime
					if (temp >= 0 && tickCount % setting.triggerPeriod == 0L) action()
				}

				else -> Unit
			}
		if (!wasPress) tickCount = 0
	}

	val asTexts: List<Text>
		get() {
			val list = LinkedList<Text>()
			keys.forEach {
				if (it.code > 8)
					list.addLast(literal("").append(InputUtil.fromKeyCode(it.code, 0).localizedText))
				else
					list.addLast(literal("").append(InputUtil.Type.MOUSE.createFromCode(it.code).localizedText))
			}
			return list
		}

	val asTranslatableKey: List<String>
		get() {
			val list = LinkedList<String>()
			keys.forEach {
				if (it.code > 8)
					list.addLast(InputUtil.fromKeyCode(it.code, 0).translationKey)
				else
					list.addLast(InputUtil.Type.MOUSE.createFromCode(it.code).translationKey)
			}
			return list
		}

	override fun matched(regex: Regex): Boolean {
		return regex.run {
			asTexts.forEach { if (this.containsMatchIn(it.string)) return@run true }
			asTranslatableKey.forEach { if (this.containsMatchIn(it)) return@run true }
			setting matched regex
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
		setting.copyOf(defaultSetting)
		setKey(*defaultKeys.toTypedArray())
	}

	fun rest() {
		wasPress = false
		tickCount = 0
	}

	fun copyOf(target: KeyBind): Boolean {
		var valueChange = setting.copyOf(target.setting)
		if (this.setKey(*target.keys.toTypedArray())) valueChange = true
		if (action != target.action) {
			action = target.action
			valueChange = true
		}
		return valueChange
	}

	override fun serialization(): SerializeElement {
		return serializeObject {
			"keys" - serializeArray(asTranslatableKey)
			"setting" - setting.serialization()
		}
	}

	override fun deserialization(serializeElement: SerializeElement) {
		try {
			val obj = serializeElement.asObject
			keys.clear()
			obj["keys"]!!.asArray.forEach {
				keys.add(KeyCode.fromCode(InputUtil.fromTranslationKey(it.asString).code))
			}
			setting.deserialization(obj["setting"]!!)
			onChange(this)
		} catch (e: Exception) {
			keys.clear()
			keys.addAll(defaultKeys.toSet())
			setting.copyOf(defaultSetting)
			log.error(e)
		}
	}
}