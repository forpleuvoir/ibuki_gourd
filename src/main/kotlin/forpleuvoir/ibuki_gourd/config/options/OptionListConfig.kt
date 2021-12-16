package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IBaseValueConfig
import forpleuvoir.ibuki_gourd.config.IOptionListConfigItem
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 OptionListConfig

 * 创建时间 2021/12/12 14:55

 * @author forpleuvoir

 */
class OptionListConfig(override val name: String, override val remark: String, override val defaultValue: IOptionListConfigItem) : ConfigBase(),
	IBaseValueConfig<IOptionListConfigItem> {
	override val type: ConfigType
		get() = ConfigType.OPTION_LIST

	private var value: IOptionListConfigItem = defaultValue

	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		try {
			if (jsonElement.isJsonPrimitive) {
				this.value = defaultValue.fromString(jsonElement.asString)
			} else {
				log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			}
		} catch (e: Exception) {
			log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			log.error(e)
		}
	}

	override val asJsonElement: JsonElement
		get() = JsonPrimitive(getValue().name)

	override val isDefaultValue: Boolean
		get() = getValue() != defaultValue

	override fun resetDefaultValue() {
		setValue(defaultValue)
	}

	override fun getValue(): IOptionListConfigItem {
		return value
	}

	override fun setValue(value: IOptionListConfigItem) {
		val oldValue = this.value
		this.value = value
		if (oldValue != this.value)
			this.onValueChange()
	}
}