package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import forpleuvoir.ibuki_gourd.utils.color.Color4i.Companion.WHITE
import forpleuvoir.ibuki_gourd.utils.color.IColor


/**
 * 颜色配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigColor

 * 创建时间 2021/12/9 19:11

 * @author forpleuvoir

 */
class ConfigColor(override val name: String, override val remark: String = "$name.remark", override val defaultValue: IColor<*> = WHITE) : ConfigBase(),
	IConfigBaseValue<IColor<*>> {

	private var value: IColor<*> = defaultValue

	override val type: ConfigType
		get() = ConfigType.COLOR

	override fun setValue(value: IColor<*>) {
		val oldValue = this.value
		this.value = value
		if (oldValue != this.value) {
			this.onValueChange()
		}
	}

	override fun matched(regex: Regex): Boolean {
		return if (regex.containsMatchIn(getValue().hexString)) true else super.matched(regex)
	}

	override fun getValue(): IColor<*> {
		return value
	}

	override val isDefaultValue: Boolean
		get() = value == defaultValue

	override fun resetDefaultValue() {
		setValue(defaultValue)
	}


	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		try {
			value.setValueFromJsonElement(jsonElement)
		} catch (e: Exception) {
			log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			log.error(e)
		}
	}

	override val asJsonElement: JsonElement
		get() = value.asJsonElement

}