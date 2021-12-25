package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.config.IConfigGroup
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigGroup

 * 创建时间 2021/12/12 15:09

 * @author forpleuvoir

 */
class ConfigGroup(override val name: String, override val remark: String = "$name.remark", override val defaultValue: List<ConfigBase>) :
	ConfigBase(),
	IConfigBaseValue<List<ConfigBase>>, IConfigGroup<ConfigBase> {

	override val type: ConfigType
		get() = ConfigType.Group

	private var value = ArrayList<ConfigBase>(defaultValue)

	override fun setValueFromJsonElement(jsonElement: JsonElement) {

	}

	override val asJsonElement: JsonElement
		get() = JsonPrimitive("")


	override val isDefaultValue: Boolean
		get() {
			this.value.forEach {
				if (!it.isDefaultValue) return false
			}
			return true
		}

	override fun resetDefaultValue() {
		this.value.forEach {
			it.resetDefaultValue()
		}
	}

	override fun getValue(): List<ConfigBase> {
		return ArrayList(value)
	}

	override fun matched(regex: Regex): Boolean {
		getValue().forEach {
			if (it.matched(regex)) return true
		}
		return super.matched(regex)
	}

	override fun setValue(value: List<ConfigBase>) {
		if (this.value != value) {
			this.value = ArrayList(value)
			this.onValueChange()
		}
	}


	override fun getValueItem(key: String): ConfigBase? {
		return this.value.find { it.name == key }
	}

	override fun getKeys(): Set<String> {
		val keys = HashSet<String>()
		this.value.forEach {
			keys.add(it.name)
		}
		return keys
	}


}