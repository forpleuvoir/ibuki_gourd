package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.options.gui.WrapperColor
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
open class ConfigColor(
	override val name: String,
	override val remark: String = "$name.remark",
	final override val defaultValue: IColor<out Number> = WHITE
) :
	ConfigBase(),
	IConfigBaseValue<IColor<out Number>> {

	private var value: IColor<out Number> = IColor.copy(defaultValue)

	override val type: ConfigType
		get() = ConfigType.COLOR

	override fun setValue(value: IColor<out Number>) {
		val oldValue = this.value
		this.value = IColor.copy(value)
		if (oldValue != this.value) {
			this.onValueChange()
		}
	}

	override fun matched(regex: Regex): Boolean {
		return if (regex.containsMatchIn(getValue().hexString)) true else super.matched(regex)
	}

	override fun getValue(): IColor<out Number> {
		return IColor.copy(value)
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


	override fun wrapper(x: Int, y: Int, width: Int, height: Int): ConfigWrapper<ConfigColor> {
		return WrapperColor(this, x, y, width, height)
	}
}