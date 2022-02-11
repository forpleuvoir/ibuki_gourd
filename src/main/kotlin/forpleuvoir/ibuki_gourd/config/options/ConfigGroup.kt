package forpleuvoir.ibuki_gourd.config.options

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.config.IConfigType
import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.gui.DialogConfigGroup
import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigGroup

 * 创建时间 2021/12/12 15:09

 * @author forpleuvoir

 */
open class ConfigGroup(
	override val name: String,
	override val remark: String = "$name.remark",
	final override val defaultValue: List<IConfigBase>
) : ConfigBase(), IConfigGroup {

	override val type: IConfigType
		get() = ConfigType.Group

	private var value = ArrayList<IConfigBase>(defaultValue)

	override fun init() {
		getValue().forEach {
			it.init()
		}
	}

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
			val json = JsonObject()
			this.value.forEach {
				json.add(it.name, it.asJsonElement)
			}
			return json
		}


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

	override fun getValue(): List<IConfigBase> {
		return ArrayList(value)
	}

	override fun matched(regex: Regex): Boolean {
		getValue().forEach {
			if (it.matched(regex)) return true
		}
		return super.matched(regex)
	}

	override fun setValue(value: List<IConfigBase>) {
		if (this.value != value) {
			this.value = ArrayList(value)
			this.onValueChange()
		}
	}


	override fun getValueItem(key: String): IConfigBase? {
		return this.value.find { it.name == key }
	}

	override fun getKeys(): Set<String> {
		val keys = HashSet<String>()
		this.value.forEach {
			keys.add(it.name)
		}
		return keys
	}

	override fun setOnValueChanged(callback: (IConfigBase) -> Unit) {
		this.value.forEach { it.setOnValueChanged(callback) }
	}

	override fun wrapper(x: Int, y: Int, width: Int, height: Int): ConfigWrapper {
		return object : ConfigWrapper(this, x, y, width, height) {
			override fun initWidget() {
				addDrawableChild(
					Button(
						x = x,
						y = y,
						width = width,
						height = height,
						message = config.displayName
					) {
						ScreenBase.openScreen(
							DialogConfigGroup(
								config = config,
								group = this@ConfigGroup,
								dialogWidth = 330,
								parent = ScreenBase.current
							)
					)
				})
			}
		}
	}


}