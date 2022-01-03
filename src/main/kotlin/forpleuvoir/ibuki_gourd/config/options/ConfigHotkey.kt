package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.options.gui.ButtonConfigHotkey
import forpleuvoir.ibuki_gourd.config.options.gui.WrapperHotKey
import forpleuvoir.ibuki_gourd.keyboard.KeyBind
import forpleuvoir.ibuki_gourd.keyboard.KeyEnvironment
import forpleuvoir.ibuki_gourd.keyboard.KeyboardUtil
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import net.minecraft.client.gui.widget.ClickableWidget


/**
 * 按键绑定配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigHotkey

 * 创建时间 2021/12/17 22:07

 * @author forpleuvoir

 */
open class ConfigHotkey(override val name: String, override val remark: String = "$name.remark", final override val defaultValue: KeyBind) :
	ConfigBase(),
	IConfigBaseValue<KeyBind> {
	override val type: ConfigType
		get() = ConfigType.HOTKEY
	private var value: KeyBind = KeyBind().apply { copyOf(defaultValue) }


	fun initKeyBind() {
		KeyboardUtil.registerKeyBind(value)
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

	override fun matched(regex: Regex): Boolean {
		return if (regex.run {
				value.asTexts.forEach {
					if (this.containsMatchIn(it.string)) return@run true
				}
				false
			}) true
		else super.matched(regex)
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
		return KeyBind().apply { copyOf(this@ConfigHotkey.value) }
	}

	fun setKeyEnvironment(keyEnvironment: KeyEnvironment) {
		if (value.keyEnvironment != keyEnvironment) {
			this.value.keyEnvironment = keyEnvironment
			this.onValueChange()
		}
	}

	override fun setValue(value: KeyBind) {
		if (this.value.copyOf(value)) {
			this.onValueChange()
		}
	}

	override fun wrapper(x: Int, y: Int, width: Int, height: Int): ConfigWrapper<ConfigHotkey> {
		return WrapperHotKey(x = x, y = y, width = width, height = height, config = this)
	}
}