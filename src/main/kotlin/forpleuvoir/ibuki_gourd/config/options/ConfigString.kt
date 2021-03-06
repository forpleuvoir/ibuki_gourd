package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigType
import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.options.gui.WidgetConfigString
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang


/**
 * 字符串配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigString

 * 创建时间 2021/12/9 19:06

 * @author forpleuvoir

 */
open class ConfigString(
	override val name: String,
	override val remark: String = "$name.remark",
	final override val defaultValue: String
) : ConfigBase(), IConfigString {

	private var value: String = defaultValue

	override val type: IConfigType
		get() = ConfigType.STRING


	override val isDefaultValue: Boolean
		get() = getValue() == defaultValue

	override fun resetDefaultValue() {
		setValue(defaultValue)
	}

	override fun getValue(): String {
		return value
	}

	override fun setValue(value: String) {
		val oldValue = this.value
		this.value = value
		if (oldValue != this.value) {
			this.onValueChange()
		}
	}

	override fun matched(regex: Regex): Boolean {
		return if (regex.containsMatchIn(getValue())) true else super.matched(regex)
	}

	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		try {
			if (jsonElement.isJsonPrimitive) {
				this.value = jsonElement.asString
			} else {
				log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			}
		} catch (e: Exception) {
			log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			log.error(e)
		}
	}

	override val asJsonElement: JsonElement
		get() = JsonPrimitive(getValue())

	override fun wrapper(x: Int, y: Int, width: Int, height: Int): ConfigWrapper {
		val widget = WidgetConfigString(
			x = x + 1,
			y = y + 1,
			width = width - 2,
			height = height - 2,
			config = this,
			string = this
		)
		return object : ConfigWrapper(this, x, y, width, height) {
			override fun initWidget() {
				addDrawableChild(widget)
			}

			override fun tick() {
				widget.tick()
			}
		}
	}
}