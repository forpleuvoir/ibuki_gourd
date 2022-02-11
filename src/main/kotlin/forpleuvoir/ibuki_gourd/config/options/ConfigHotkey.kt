package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigType
import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.options.gui.WrapperHotKey
import forpleuvoir.ibuki_gourd.keyboard.KeyBind
import forpleuvoir.ibuki_gourd.keyboard.KeyEnvironment
import forpleuvoir.ibuki_gourd.keyboard.KeyboardUtil
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang


/**
 * 按键绑定配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigHotkey

 * 创建时间 2021/12/17 22:07

 * @author forpleuvoir

 */
open class ConfigHotkey(
	override val name: String,
	override val remark: String = "$name.remark",
	final override val defaultValue: KeyBind
) : ConfigBase(), IConfigHotkey {
	override val type: IConfigType
		get() = ConfigType.HOTKEY
	protected open val keyBind: KeyBind = KeyBind().apply { copyOf(defaultValue) }

	override fun init() {
		initKeyBind()
	}

	override fun initKeyBind() {
		KeyboardUtil.registerKeyBind(keyBind)
	}

	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		try {
			if (jsonElement.isJsonObject) {
				this.keyBind.setValueFromJsonElement(jsonElement)
			} else {
				log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			}
		} catch (e: Exception) {
			log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			log.error(e)
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

	override fun setKeyCallback(callback: () -> Unit) {
		this.keyBind.callback = callback
	}

	override val asJsonElement: JsonElement
		get() = this.keyBind.asJsonElement

	override val isDefaultValue: Boolean
		get() = this.keyBind == defaultValue

	override fun resetDefaultValue() {
		this.setValue(defaultValue)
	}

	override fun getValue(): KeyBind {
		return KeyBind().apply { copyOf(this@ConfigHotkey.keyBind) }
	}

	override fun setKeyEnvironment(keyEnvironment: KeyEnvironment) {
		if (keyBind.keyEnvironment != keyEnvironment) {
			this.keyBind.keyEnvironment = keyEnvironment
			this.onValueChange()
		}
	}

	override fun setValue(value: KeyBind) {
		if (this.keyBind.copyOf(value)) {
			this.onValueChange()
		}
	}

	override fun wrapper(x: Int, y: Int, width: Int, height: Int): ConfigWrapper {
		return WrapperHotKey(x = x, y = y, width = width, height = height, config = this, hotkey = this)
	}
}