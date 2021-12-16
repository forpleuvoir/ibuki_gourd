package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IBaseValueConfig
import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang


/**
 * 布尔配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 BooleanConfig

 * 创建时间 2021/12/9 18:29

 * @author forpleuvoir

 */
class BooleanConfig(override val name: String, override val remark: String, override val defaultValue: Boolean) : ConfigBase(),
	IBaseValueConfig<Boolean> {

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


}