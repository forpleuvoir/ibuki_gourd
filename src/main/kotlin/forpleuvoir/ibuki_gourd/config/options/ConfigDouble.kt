package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import net.minecraft.util.math.MathHelper
import kotlin.math.max


/**
 * 浮点数配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigDouble

 * 创建时间 2021/12/9 19:05

 * @author forpleuvoir

 */
class ConfigDouble(
	override val name: String,
	override val remark: String = "$name.remark",
	override val defaultValue: Double = 0.0,
	val minValue: Double = Double.MIN_VALUE,
	val maxValue: Double = Double.MAX_VALUE
) : ConfigBase(),
	IConfigBaseValue<Double> {

	private var value: Double = MathHelper.clamp(defaultValue, minValue, maxValue)

	override val type: ConfigType
		get() = ConfigType.DOUBLE


	override val isDefaultValue: Boolean
		get() = getValue() == defaultValue

	override fun resetDefaultValue() {
		setValue(defaultValue)
	}

	override fun getValue(): Double {
		return value
	}

	override fun setValue(value: Double) {
		val oldValue = this.value
		this.value = value
		this.value = MathHelper.clamp(this.value, minValue, maxValue)
		if (oldValue != this.value) {
			this.onValueChange()
		}
	}

	override fun matched(regex: Regex): Boolean {
		return if (regex.containsMatchIn(getValue().toString())) true else super.matched(regex)
	}

	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		try {
			if (jsonElement.isJsonPrimitive) {
				this.value = jsonElement.asDouble
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