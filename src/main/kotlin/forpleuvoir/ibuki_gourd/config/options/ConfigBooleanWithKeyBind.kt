package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigKeyBind
import forpleuvoir.ibuki_gourd.config.IConfigType
import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.options.gui.ButtonConfigBoolean
import forpleuvoir.ibuki_gourd.config.options.gui.WrapperHotKey
import forpleuvoir.ibuki_gourd.keyboard.KeyBind
import forpleuvoir.ibuki_gourd.keyboard.KeyEnvironment
import forpleuvoir.ibuki_gourd.keyboard.KeyboardUtil
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import forpleuvoir.ibuki_gourd.utils.Message

/**
 * 带热键的布尔配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigBooleanWithKeyBind

 * 创建时间 2022/2/11 15:20

 * @author forpleuvoir

 */
open class ConfigBooleanWithKeyBind(
	override val name: String,
	override val remark: String = "$name.remark",
	final override val defaultValue: Boolean,
	private val defaultKeyBind: KeyBind = KeyBind()
) : ConfigBase(), IConfigBoolean, IConfigKeyBind {

	override val type: IConfigType
		get() = ConfigType.BOOLEAN_WITH_KEY_BIND

	protected open val onKeyPress: () -> Unit = {
		this.toggle()
		val status = if (getValue()) IbukiGourdLang.On.tText() else IbukiGourdLang.Off.tText()
		Message.showInfo(displayName.append(": ").append(status))
	}


	protected open val keyBind: KeyBind = KeyBind().apply {
		copyOf(defaultKeyBind)
		callback = onKeyPress
	}

	override fun init() {
		initKeyBind()
	}

	protected open val hotkey: IConfigHotkey = object : IConfigHotkey {

		override fun setKeyCallback(callback: () -> Unit) {}

		override fun initKeyBind() {
			KeyboardUtil.registerKeyBind(keyBind)
		}

		override fun setKeyEnvironment(keyEnvironment: KeyEnvironment) {
			if (keyBind.keyEnvironment != keyEnvironment) {
				this@ConfigBooleanWithKeyBind.keyBind.keyEnvironment = keyEnvironment
				this@ConfigBooleanWithKeyBind.onValueChange()
			}
		}

		override val defaultValue: KeyBind
			get() = defaultKeyBind

		override fun getValue(): KeyBind = KeyBind().apply { copyOf(keyBind) }

		override fun setValue(value: KeyBind) {
			if (this@ConfigBooleanWithKeyBind.keyBind.copyOf(value)) {
				this@ConfigBooleanWithKeyBind.keyBind.callback = onKeyPress
				this@ConfigBooleanWithKeyBind.onValueChange()
			}
		}

	}

	protected open var booleanValue: Boolean = defaultValue

	override fun initKeyBind() {
		hotkey.initKeyBind()
	}

	override fun resetDefaultValue() {
		if (isDefaultValue) return
		this.setValue(defaultValue)
		hotkey.setValue(defaultKeyBind)
	}

	override fun wrapper(x: Int, y: Int, width: Int, height: Int): ConfigWrapper {
		return object : ConfigWrapper(this, x, y, width, height) {
			override fun initWidget() {
				addDrawableChild(
					ButtonConfigBoolean(
						x = x,
						y = y,
						width = 40,
						height = height,
						config = this@ConfigBooleanWithKeyBind
					)
				)
				addDrawableChild(
					WrapperHotKey(
						x = x + 40 + 2,
						y = y,
						width = width - 40 - 2,
						height = height,
						config = this@ConfigBooleanWithKeyBind,
						hotkey = this@ConfigBooleanWithKeyBind.hotkey
					).apply { initWidget() }
				)
			}
		}
	}

	override fun getValue(): Boolean {
		return booleanValue
	}

	override fun setValue(value: Boolean) {
		val oldValue = this.booleanValue
		this.booleanValue = value
		if (oldValue != this.booleanValue) {
			this.onValueChange()
		}
	}


	override val isDefaultValue: Boolean
		get() = getValue() == defaultValue && hotkey.getValue() == defaultKeyBind


	override fun setKeyEnvironment(keyEnvironment: KeyEnvironment) {
		if (keyBind.keyEnvironment != keyEnvironment) {
			this.keyBind.keyEnvironment = keyEnvironment
			this.onValueChange()
		}
	}

	override fun matched(regex: Regex): Boolean {
		return if (regex.run {
				keyBind.asTexts.forEach {
					if (this.containsMatchIn(it.string)) return@run true
				}
				false
			}) true
		else super.matched(regex)
	}


	override val asJsonElement: JsonElement
		get() = JsonObject().apply {
			addProperty("value", booleanValue)
			add("keyBind", keyBind.asJsonElement)
		}

	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		try {
			if (jsonElement.isJsonObject) {
				this.booleanValue = jsonElement.asJsonObject["value"].asBoolean
				this.keyBind.setValueFromJsonElement(jsonElement.asJsonObject["keyBind"].asJsonObject)
			} else {
				log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			}
		} catch (e: Exception) {
			log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			log.error(e)
		}
	}

}