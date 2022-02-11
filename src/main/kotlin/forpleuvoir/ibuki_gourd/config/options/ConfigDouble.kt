package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigType
import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.options.gui.WidgetSliderConfigDouble
import forpleuvoir.ibuki_gourd.config.options.gui.WrapperNumber
import forpleuvoir.ibuki_gourd.gui.widget.WidgetSliderNumber
import forpleuvoir.ibuki_gourd.gui.widget.WidgetTextFieldDouble
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import forpleuvoir.ibuki_gourd.utils.clamp
import net.minecraft.client.gui.widget.TextFieldWidget


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
	override val minValue: Double = Double.MIN_VALUE,
	override val maxValue: Double = Double.MAX_VALUE
) : ConfigBase(), IConfigDouble {

	private var value: Double = defaultValue.clamp(minValue, maxValue).toDouble()

	override val type: IConfigType
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
		this.value = this.value.clamp(minValue, maxValue).toDouble()
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

	override fun wrapper(x: Int, y: Int, width: Int, height: Int): ConfigWrapper {
		return object : WrapperNumber(this, x, y, width, height) {
			override val slider: WidgetSliderNumber
				get() = WidgetSliderConfigDouble(0, 0, 0, height, config = config, double = this@ConfigDouble)
			override val field: TextFieldWidget
				get() = WidgetTextFieldDouble(0, 0, 0, height - 2, this@ConfigDouble.getValue()).apply {
					setConsumer {
						it?.let { this@ConfigDouble.setValue(it) }
					}
				}
		}
	}

}