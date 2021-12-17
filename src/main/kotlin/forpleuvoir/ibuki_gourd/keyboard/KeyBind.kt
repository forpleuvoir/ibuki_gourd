package forpleuvoir.ibuki_gourd.keyboard

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import forpleuvoir.ibuki_gourd.common.IJsonData
import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.event.util.KeyEnvironment
import forpleuvoir.ibuki_gourd.mod.utils.IbukiGourdLang
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import kotlin.jvm.Throws


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.keyboard

 * 文件名 KeyBind

 * 创建时间 2021/12/17 15:03

 * @author forpleuvoir

 */
class KeyBind(vararg key: Int, var keyEnvironment: KeyEnvironment = KeyEnvironment.IN_GAME, var callback: () -> Unit = {}) : IJsonData {
	val keys: HashSet<Int> = HashSet(key.toSet())

	fun callbackHandler(key: Set<Int>) {
		if (key.containsAll(keys)) {
			val keyEnv = if (MinecraftClient.getInstance().currentScreen == null) KeyEnvironment.IN_GAME else KeyEnvironment.IN_SCREEN
			if (keyEnv == this.keyEnvironment || this.keyEnvironment == KeyEnvironment.ALL)
				callback.invoke()
		}
	}

	val asTexts: List<Text>
		get() {
			val list = ArrayList<Text>()
			keys.forEach {
				list.add(InputUtil.fromKeyCode(it, 0).localizedText)
			}
			return list
		}

	val asTranslatableKey: List<String>
		get() {
			val list = ArrayList<String>()
			keys.forEach {
				list.add(InputUtil.fromKeyCode(it, 0).translationKey)
			}
			return list
		}

	@Throws(Exception::class)
	override fun setValueFromJsonElement(jsonElement: JsonElement) {
		val jsonObject = jsonElement.asJsonObject
		this.keys.clear()
		jsonObject["keys"].asJsonArray.forEach {
			this.keys.add(InputUtil.fromTranslationKey(it.asString).code)
		}
		this.keyEnvironment = KeyEnvironment.valueOf(jsonObject["keyEnvironment"].asString)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as KeyBind

		if (keyEnvironment != other.keyEnvironment) return false
		if (keys != other.keys) return false

		return true
	}

	override fun hashCode(): Int {
		var result = keyEnvironment.hashCode()
		result = 31 * result + keys.hashCode()
		return result
	}

	override val asJsonElement: JsonElement
		get() {
			val jsonObject = JsonObject()
			val jsonArray = JsonArray()
			asTranslatableKey.forEach { jsonArray.add(it) }
			jsonObject.add("keys", jsonArray)
			jsonObject.addProperty("keyEnvironment", keyEnvironment.name)
			return jsonObject
		}


}