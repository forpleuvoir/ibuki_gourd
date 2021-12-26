package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.config.options.gui.ButtonConfigBoolean
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import net.minecraft.client.gui.widget.ClickableWidget


/**
 * 布尔配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigBoolean

 * 创建时间 2021/12/9 18:29

 * @author forpleuvoir

 */
class ConfigBoolean(override val name: String, override val remark: String = "$name.remark", override val defaultValue: Boolean) : ConfigBase(),
	IConfigBaseValue<Boolean> {

	private var value: Boolean = defaultValue

	override val type: ConfigType
		get() = ConfigType.BOOLEAN


	override fun getValue(): Boolean {
		return value
	}

	fun toggle() {
		this.setValue(!getValue())
	}

	override fun setValue(value: Boolean) {
		val oldValue = this.value
		this.value = value
		if (oldValue != this.value) {
			this.onValueChange()
		}
	}

	override val asJsonElement: JsonElement
		get() = JsonPrimitive(getValue())

	override val isDefaultValue: Boolean
		get() = this.value == this.defaultValue

	override fun resetDefaultValue() {
		setValue(defaultValue)
	}

	override fun matched(regex: Regex): Boolean {
		return if (regex.containsMatchIn(getValue().toString())) true else super.matched(regex)
	}

	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		try {
			if (jsonElement.isJsonPrimitive) {
				this.value = jsonElement.asBoolean
			} else {
				log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			}
		} catch (e: Exception) {
			log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			log.error(e)
		}
	}

	override fun wrapper(x: Int, y: Int, width: Int, height: Int): ClickableWidget {
		return ButtonConfigBoolean(x = x, y = y, width = width, height = height, config = this)
	}

}