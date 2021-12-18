package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import net.minecraft.util.math.MathHelper


/**
 * 整数配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigInt

 * 创建时间 2021/12/9 18:55

 * @author forpleuvoir

 */
class ConfigInt(
	override val name: String,
	override val remark: String = "$name.remark",
	override val defaultValue: Int = 0,
	val minValue: Int = Int.MIN_VALUE,
	val maxValue: Int = Int.MAX_VALUE
) : ConfigBase(),
	IConfigBaseValue<Int> {

	private var value: Int = MathHelper.clamp(defaultValue, minValue, maxValue)

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
		this.value = MathHelper.clamp(this.value, minValue, maxValue)
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