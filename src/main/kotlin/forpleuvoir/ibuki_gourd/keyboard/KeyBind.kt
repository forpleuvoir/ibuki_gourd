package forpleuvoir.ibuki_gourd.keyboard

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import forpleuvoir.ibuki_gourd.common.IJsonData
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import java.util.*


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.keyboard

 * 文件名 KeyBind

 * 创建时间 2021/12/17 15:03

 * @author forpleuvoir

 */
class KeyBind(
	vararg key: Int,
	var keyEnvironment: KeyEnvironment = KeyEnvironment.IN_GAME,
	var callback: () -> Unit = {}
) : IJsonData {
	val keys: LinkedHashSet<Int> = LinkedHashSet(key.toSet())

	fun callbackHandler(key: Set<Int>): Boolean {
		if (keys.isEmpty()) return false
		if (key.size == keys.size && key.containsAll(keys)) {
			val keyEnv =
				if (MinecraftClient.getInstance().currentScreen == null) KeyEnvironment.IN_GAME else KeyEnvironment.IN_SCREEN
			if (keyEnv == this.keyEnvironment || this.keyEnvironment == KeyEnvironment.ALL) {
				callback.invoke()
				return true
			}
		}
		return false
	}

	val asTexts: List<Text>
		get() {
			val list = LinkedList<Text>()
			keys.forEach {
				if (it > 8)
					list.addLast(InputUtil.fromKeyCode(it, 0).localizedText)
				else
					list.addLast(InputUtil.Type.MOUSE.createFromCode(it).localizedText)
			}
			return list
		}

	val asTranslatableKey: List<String>
		get() {
			val list = LinkedList<String>()
			keys.forEach {
				if (it > 8)
					list.addLast(InputUtil.fromKeyCode(it, 0).translationKey)
				else
					list.addLast(InputUtil.Type.MOUSE.createFromCode(it).translationKey)
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

	fun clearKey() {
		keys.clear()
	}

	fun add(keyCode: Int) {
		keys.add(keyCode)
	}

	fun copyOf(key: KeyBind): Boolean {
		var valueChange = false
		if (this.keys != key.keys) {
			this.keys.clear()
			this.keys.addAll(key.keys)
			valueChange = true
		}
		this.callback = key.callback
		if (this.keyEnvironment != key.keyEnvironment) {
			this.keyEnvironment = key.keyEnvironment
			valueChange = true
		}
		return valueChange
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