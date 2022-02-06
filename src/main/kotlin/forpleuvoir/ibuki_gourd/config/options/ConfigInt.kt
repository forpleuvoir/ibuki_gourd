package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.options.gui.WidgetSliderConfigInt
import forpleuvoir.ibuki_gourd.config.options.gui.WrapperNumber
import forpleuvoir.ibuki_gourd.gui.widget.WidgetSliderNumber
import forpleuvoir.ibuki_gourd.gui.widget.WidgetTextFieldInt
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import forpleuvoir.ibuki_gourd.utils.clamp
import net.minecraft.client.gui.widget.TextFieldWidget


/**
 * 整数配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigInt

 * 创建时间 2021/12/9 18:55

 * @author forpleuvoir

 */
open class ConfigInt(
	override val name: String,
	override val remark: String = "$name.remark",
	final override val defaultValue: Int = 0,
	val minValue: Int = Int.MIN_VALUE,
	val maxValue: Int = Int.MAX_VALUE
) : ConfigBase(),
	IConfigBaseValue<Int> {

	private var value: Int = defaultValue.clamp(minValue, maxValue).toInt()

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
		this.value = this.value.clamp(minValue, maxValue).toInt()
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

	override fun wrapper(x: Int, y: Int, width: Int, height: Int): ConfigWrapper<ConfigBase> {
		return object : WrapperNumber(this, x, y, width, height) {
			override val slider: WidgetSliderNumber
				get() = WidgetSliderConfigInt(0, 0, 0, height, config = config as ConfigInt)
			override val field: TextFieldWidget
				get() = WidgetTextFieldInt(0, 0, 0, height, (config as ConfigInt).getValue()).apply {
					setConsumer {
						it?.let { this@ConfigInt.setValue(it) }
					}
				}
		}
	}
}