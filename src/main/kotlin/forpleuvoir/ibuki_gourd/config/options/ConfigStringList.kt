package forpleuvoir.ibuki_gourd.config.options

import com.google.common.collect.ImmutableList
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang


/**
 * 字符串列表配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigStringList

 * 创建时间 2021/12/9 20:17

 * @author forpleuvoir

 */
class ConfigStringList(
	override val name: String,
	override val remark: String = "$name.remark",
	override val defaultValue: ImmutableList<String>
) : ConfigBase(),
	IConfigBaseValue<List<String>> {

	private var value: ArrayList<String> = ArrayList(defaultValue)

	override val type: ConfigType
		get() = ConfigType.STRING_LIST


	override fun matched(regex: Regex): Boolean {
		return if (regex.run {
				getValue().forEach {
					if (containsMatchIn(it)) return@run true
				}
				false
			}) true
		else super.matched(regex)
	}

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

	fun add(string: String) {
		this.value.add(string)
		this.onValueChange()
	}

	fun set(index: Int, string: String) {
		if (this.value[index] != string) {
			this.value[index] = string
			this.onValueChange()
		}
	}

	fun remove(index: Int) {
		if (index >= 0 && index < this.value.size) {
			this.value.removeAt(index)
			this.onValueChange()
		}
	}

	fun remove(string: String) {
		if (this.value.remove(string)) {
			this.onValueChange()
		}
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