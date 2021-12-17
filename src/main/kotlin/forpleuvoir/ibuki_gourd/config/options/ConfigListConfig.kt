package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.config.IConfigListConfig
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigListConfig

 * 创建时间 2021/12/12 15:09

 * @author forpleuvoir

 */
class ConfigListConfig(override val name: String, override val remark: String, override val defaultValue: Set<ConfigBase>) : ConfigBase(),
	IConfigBaseValue<Set<ConfigBase>>, IConfigListConfig<ConfigBase> {

	override val type: ConfigType
		get() = ConfigType.LIST

	private var value: Set<ConfigBase> = HashSet<ConfigBase>(defaultValue)

	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		try {
			if (jsonElement.isJsonObject) {
				val jsonObject = jsonElement.asJsonObject
				this.value.forEach {
					it.setValueFromJsonElement(jsonObject[it.name])
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
			val jsonObject = JsonObject()
			this.value.forEach {
				jsonObject.add(it.name, it.asJsonElement)
			}
			return jsonObject
		}

	override val isDefaultValue: Boolean
		get() = defaultValue == this.value

	override fun resetDefaultValue() {
		setValue(defaultValue)
	}

	override fun getValue(): Set<ConfigBase> {
		return value
	}

	override fun setValue(value: Set<ConfigBase>) {
		if (this.value != value) {
			this.value = HashSet<ConfigBase>(value)
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