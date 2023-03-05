package com.forpleuvoir.ibukigourd.input

import com.forpleuvoir.ibukigourd.api.Tickable
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
	vararg keyCodes: Int,
	private val defaultSetting: KeyBindSetting,
	private val action: KeyBind.() -> Unit
) : Tickable, Resettable, Notifiable<KeyBind>, Matchable, Serializable, Deserializable {

	private val log = logger()

	val uuid: UUID = UUID.randomUUID()

	private val defaultKeys: MutableList<Int> = keyCodes.toMutableList()

	var setting: KeyBindSetting = keyBindSetting().apply { copyOf(defaultSetting) }

	val keys: MutableList<Int> = ArrayList(defaultKeys)

	/**
	 * 当前按键是否被按下
	 */
	var wasPress: Boolean = false
		private set

	/**
	 * 是否需要触发
	 */
	private var mustTrigger: Boolean = false

	/**
	 * 按键被按下的时间，释放时会清空
	 */
	private var tickCount: Int = 0

	fun setKey(vararg keyCodes: Int) {
		keys.clear()
		keys.addAll(keyCodes.toMutableList())
		onChange(this)
	}

	fun onKeyPress(beforeKeyCode: List<Int>, currentKeyCode: List<Int>): NextAction {
		if (currentKeyCode.isEmpty() || setting.environment.envMatch()) {
			wasPress = false
			return NextAction.Continue
		}
		wasPress = if (setting.exactMatch) {
			keys exactMatch currentKeyCode
		} else {
			keys == currentKeyCode || currentKeyCode.containsAll(currentKeyCode)
		}
		if (wasPress) {
			return setting.nextAction
		}
		return NextAction.Continue
	}

	fun onKeyRelease(beforeKeyCode: List<Int>, currentKeyCode: List<Int>): NextAction {
		if (beforeKeyCode.isEmpty() || setting.environment.envMatch()) {
			wasPress = false
			return NextAction.Continue
		}
		val isMatched = if (setting.exactMatch) {
			keys exactMatch beforeKeyCode
		} else {
			keys == beforeKeyCode || beforeKeyCode.containsAll(beforeKeyCode)
		}
		if (isMatched) {
			if (setting.triggerMode == KeyTriggerMode.OnRelease || setting.triggerMode == KeyTriggerMode.BOTH) {

			}
			return setting.nextAction
		}
		return NextAction.Continue
	}

	override fun tick() {
		if (wasPress) tickCount++
		else tickCount = 0
	}

	val asTexts: List<Text>
		get() {
			val list = LinkedList<Text>()
			keys.forEach {
				if (it > 8)
					list.addLast(literal("").append(InputUtil.fromKeyCode(it, 0).localizedText))
				else
					list.addLast(literal("").append(InputUtil.Type.MOUSE.createFromCode(it).localizedText))
			}
			return list
		}

	val asTranslatableKey: List<String>
		get() {
			val list = LinkedList<String>()
			keys.forEach {
				if (it > 8)
					list.addLast(InputUtil.fromKeyCode(it, 0).translationKey)
				else
					list.addLast(InputUtil.Type.MOUSE.createFromCode(it).translationKey)
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

	override fun isDefault(): Boolean = defaultKeys exactMatch keys && setting == defaultSetting

	override fun restDefault() {
		setting = defaultSetting
		setKey(*defaultKeys.toIntArray())
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
				keys.add(InputUtil.fromTranslationKey(it.asString).code)
			}
			setting.deserialization(obj["setting"]!!)
			onChange(this)
		} catch (e: Exception) {
			keys.clear()
			keys.addAll(defaultKeys.toSet())
			setting = defaultSetting
			log.error(e)
		}
	}
}