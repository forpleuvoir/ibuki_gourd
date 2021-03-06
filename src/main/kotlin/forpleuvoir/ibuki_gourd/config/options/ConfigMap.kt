package forpleuvoir.ibuki_gourd.config.options

import com.google.common.collect.ImmutableMap
import com.google.gson.JsonElement
import forpleuvoir.ibuki_gourd.config.ConfigType
import forpleuvoir.ibuki_gourd.config.IConfigType
import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper
import forpleuvoir.ibuki_gourd.config.gui.DialogConfigMap
import forpleuvoir.ibuki_gourd.gui.button.Button
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod.mc
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import forpleuvoir.ibuki_gourd.utils.JsonUtil.toJsonObject
import forpleuvoir.ibuki_gourd.utils.text
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 ConfigMap

 * 创建时间 2021/12/17 22:55

 * @author forpleuvoir

 */
open class ConfigMap(
	override val name: String,
	override val remark: String = "$name.remark",
	final override val defaultValue: Map<String, String>
) : ConfigBase(), IConfigMap {

	protected open val value: LinkedHashMap<String, String> = LinkedHashMap(defaultValue)

	override val type: IConfigType
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

	override fun remove(key: String) {
		if (key.contains(key)) {
			this.value.remove(key)
			this.onValueChange()
		}
	}

	override fun rename(old: String, new: String) {
		if (this.value.containsKey(old)) {
			val value = this.value[old]
			value?.let {
				this.value.remove(old)
				this.value[new] = it
				this.onValueChange()
			}
		}
	}

	override fun reset(oldKey: String, newKey: String, value: String) {
		if (this.value.containsKey(oldKey)) {
			if (oldKey != newKey) rename(oldKey, newKey)
			this.value[newKey] = value
			this.onValueChange()
		}
	}

	override operator fun set(key: String, value: String) {
		val oldValue: String? = this.value[key]
		this.value[key] = value
		if (oldValue != value) {
			this.onValueChange()
		}
	}

	override operator fun get(key: String): String? {
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

	override fun wrapper(x: Int, y: Int, width: Int, height: Int): ConfigWrapper {
		val hoverTexts = ArrayList<Text>()
		getValue().forEach {
			hoverTexts.add(mc.textRenderer.trimToWidth("${it.key}:${it.value}", 360).text)
		}
		return object : ConfigWrapper(this, x, y, width, height) {
			override fun initWidget() {
				addDrawableChild(Button(x = x, y = 0, width = width, height = height, message = config.displayName) {
					ScreenBase.openScreen(
						DialogConfigMap(
							config = config,
							map = this@ConfigMap,
							dialogWidth = 360,
							parent = ScreenBase.current
						)
					)
				}.apply { setHoverText(hoverTexts) })
			}
		}
	}
}