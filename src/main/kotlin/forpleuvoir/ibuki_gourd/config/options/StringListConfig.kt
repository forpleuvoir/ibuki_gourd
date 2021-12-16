package forpleuvoir.ibuki_gourd.config.options

import com.google.common.collect.ImmutableList
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IBaseValueConfig
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang


/**
 * 字符串列表配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 StringListConfig

 * 创建时间 2021/12/9 20:17

 * @author forpleuvoir

 */
class StringListConfig(override val name: String, override val remark: String, override val defaultValue: ImmutableList<String>) : ConfigBase(),
	IBaseValueConfig<List<String>> {

	private var value: ArrayList<String> = ArrayList(defaultValue)

	override val type: ConfigType
		get() = ConfigType.STRING_LIST

	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		try {
			if (jsonElement.isJsonArray) {
				val jsonArray = jsonElement.asJsonArray
				value.clear()
				jsonArray.forEach {
					value.add(it.asString)
				}
			} else {
				log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			}
		} catch (e: Exception) {
			log.warn(IbukiGourdLang.SetFromJsonFailed.tString(name, jsonElement))
			log.error(e)
		}
	}

	override val asJsonElement: JsonElement
		get() {
			val arr = JsonArray()
			value.forEach {
				arr.add(JsonPrimitive(it))
			}
			return arr
		}

	override val isDefaultValue: Boolean
		get() = value.toArray().contentEquals(defaultValue.toArray())


	override fun resetDefaultValue() {
		value = ArrayList(defaultValue)
	}

	override fun getValue(): List<String> {
		return ImmutableList.copyOf(value)
	}

	override fun setValue(value: List<String>) {
		if (this.value != value) {
			this.value.clear()
			this.value.addAll(value)
			this.onValueChange()
		}
	}
}