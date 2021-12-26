package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.config.IConfigOptionItem
import forpleuvoir.ibuki_gourd.config.options.gui.ButtonConfigOptions
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import net.minecraft.client.gui.widget.ClickableWidget


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigOptions

 * 创建时间 2021/12/12 14:55

 * @author forpleuvoir

 */
class ConfigOptions(
	override val name: String,
	override val remark: String = "$name.remark",
	override val defaultValue: IConfigOptionItem
) : ConfigBase(),
	IConfigBaseValue<IConfigOptionItem> {
	override val type: ConfigType
		get() = ConfigType.OPTIONS

	private var value: IConfigOptionItem = defaultValue

	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		try {
			if (jsonElement.isJsonPrimitive) {
				this.value = defaultValue.fromKey(jsonElement.asString)
			} else {
				log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			}
		} catch (e: Exception) {
			log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			log.error(e)
		}
	}

	override val asJsonElement: JsonElement
		get() = JsonPrimitive(getValue().key)

	override val isDefaultValue: Boolean
		get() = getValue() == defaultValue

	override fun resetDefaultValue() {
		setValue(defaultValue)
	}

	override fun matched(regex:Regex): Boolean {
		return if (regex.run {
				containsMatchIn(getValue().displayKey.string)
						|| containsMatchIn(getValue().displayRemark.string)
						|| containsMatchIn(getValue().key)
						|| containsMatchIn(getValue().remark)
			}) true
		else super.matched(regex)
	}

	override fun getValue(): IConfigOptionItem {
		return value
	}

	override fun setValue(value: IConfigOptionItem) {
		val oldValue = this.value
		this.value = value
		if (oldValue != this.value)
			this.onValueChange()
	}

	override fun wrapper(x: Int, y: Int, width: Int, height: Int): ClickableWidget {
		return ButtonConfigOptions(x = x, y = y, width = width, height = height, config = this)
	}
}