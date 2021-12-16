package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IBaseValueConfig
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang


/**
 * 整数配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 IntegerConfig

 * 创建时间 2021/12/9 18:55

 * @author forpleuvoir

 */
class IntegerConfig(
	override val name: String,
	override val remark: String,
	override val defaultValue: Int = 0,
	private val maxValue: Int = Int.MAX_VALUE,
	private val minValue: Int = Int.MIN_VALUE
) : ConfigBase(),
	IBaseValueConfig<Int> {

	private var value: Int = defaultValue

	override val type: ConfigType
		get() = ConfigType.INTEGER


	override val isDefaultValue: Boolean
		get() = getValue() == defaultValue

	override fun resetDefaultValue() {
		setValue(defaultValue)
	}

	override fun getValue(): Int {
		return value
	}

	override fun setValue(value: Int) {
		val oldValue = this.value
		this.value = value
		this.value = this.value.coerceAtLeast(minValue)
		this.value = this.value.coerceAtMost(maxValue)
		if (oldValue != this.value) {
			this.onValueChange()
		}
	}

	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		try {
			if (jsonElement.isJsonPrimitive) {
				this.value = jsonElement.asInt
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
}