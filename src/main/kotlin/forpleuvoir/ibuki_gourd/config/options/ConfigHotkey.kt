package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.keyboard.KeyBind
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
open class ConfigHotkey(override val name: String, override val remark: String = "$name.remark", final override val defaultValue: KeyBind) : ConfigBase(),
	IConfigBaseValue<KeyBind> {
	override val type: ConfigType
		get() = ConfigType.HOTKEY
	private var value: KeyBind = defaultValue

	init {
		KeyboardUtil.registerKeyBind(this.getValue())
	}

	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		try {
			if (jsonElement.isJsonObject) {
				this.value.setValueFromJsonElement(jsonElement)
			} else {
				log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			}
		} catch (e: Exception) {
			log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			log.error(e)
		}
	}

	fun setKeyCallback(callback: () -> Unit) {
		this.value.callback = callback
	}

	override val asJsonElement: JsonElement
		get() = this.value.asJsonElement

	override val isDefaultValue: Boolean
		get() = this.value == defaultValue

	override fun resetDefaultValue() {
		this.setValue(defaultValue)
	}

	override fun getValue(): KeyBind {
		return this.value
	}

	override fun setValue(value: KeyBind) {
		if (this.value.copyOf(value)) {
			this.onValueChange()
		}
	}
}