package forpleuvoir.ibuki_gourd.config.options

import com.google.common.collect.ImmutableMap
import com.google.gson.JsonElement
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.gui.DialogConfigMap
import forpleuvoir.ibuki_gourd.config.options.gui.ButtonConfigBoolean
import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import forpleuvoir.ibuki_gourd.utils.toJsonObject
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.ClickableWidget


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigMap

 * 创建时间 2021/12/17 22:55

 * @author forpleuvoir

 */
class ConfigMap(
	override val name: String,
	override val remark: String = "$name.remark",
	override val defaultValue: Map<String, String>
) : ConfigBase(), IConfigBaseValue<Map<String, String>> {

	private val value: HashMap<String, String> = HashMap(defaultValue)

	override val type: ConfigType
		get() = ConfigType.MAP

	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		try {
			if (jsonElement.isJsonObject) {
				value.clear()
				val jsonObject = jsonElement.asJsonObject
				jsonObject.entrySet().forEach {
					value[it.key] = it.value.asString
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
		get() = getValue().toJsonObject()

	override val isDefaultValue: Boolean
		get() = getValue() == defaultValue

	override fun resetDefaultValue() {
		setValue(defaultValue)
	}

	fun remove(key: String) {
		if (key.contains(key)) {
			this.value.remove(key)
			this.onValueChange()
		}
	}

	fun rename(old: String, new: String) {
		if (this.value.containsKey(old)) {
			val value = this.value[old]
			value?.let {
				this.value.remove(old)
				this.value[new] = it
				this.onValueChange()
			}
		}
	}

	fun reset(oldKey: String, newKey: String, value: String) {
		if (this.value.containsKey(oldKey)) {
			if (oldKey != newKey) rename(oldKey, newKey)
			this.value[newKey] = value
			this.onValueChange()
		}
	}

	operator fun set(key: String, value: String) {
		val oldValue: String? = this.value[key]
		this.value[key] = value
		if (oldValue != value) {
			this.onValueChange()
		}
	}

	operator fun get(key: String): String? {
		return this.value[key]
	}

	override fun matched(regex: Regex): Boolean {
		return if (regex.run {
				getValue().forEach {
					if (containsMatchIn(it.key) || containsMatchIn(it.value)) return@run true
				}
				false
			}) true
		else super.matched(regex)
	}

	override fun getValue(): Map<String, String> {
		return ImmutableMap.copyOf(value)
	}

	override fun setValue(value: Map<String, String>) {
		if (this.value != value) {
			this.value.clear()
			this.value.putAll(value)
			this.onValueChange()
		}
	}

	override fun wrapper(x: Int, y: Int, width: Int, height: Int): ConfigWrapper<ConfigMap> {
		return object : ConfigWrapper<ConfigMap>(this, x, y, width, height) {
			override fun initWidget() {
				addDrawableChild(Button(x = x, y = 0, width = width, height = height, message = config.displayName) {
					ScreenBase.openScreen(
						DialogConfigMap(
							config = config,
							dialogWidth = 360,
							parent = ScreenBase.current
						)
					)
				})
			}
		}
	}
}