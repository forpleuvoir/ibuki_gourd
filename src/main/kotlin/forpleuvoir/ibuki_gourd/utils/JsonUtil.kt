package forpleuvoir.ibuki_gourd.utils

import com.google.gson.*


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.utils

 * 文件名 JsonUtil

 * 创建时间 2021/12/12 19:17

 * @author forpleuvoir

 */
object JsonUtil {
	val gson: Gson = GsonBuilder().setPrettyPrinting().create()


	fun toStringBuffer(obj: Any?): StringBuffer {
		return StringBuffer(gson.toJson(obj))
	}


	fun getObject(parent: JsonObject, key: String): JsonObject? {
		if (parent.has(key) && parent[key].isJsonObject) {
			return parent[key].asJsonObject
		}
		return null
	}

	/**
	 * 将对象转换成json字符串
	 *
	 * @param json 需要转换的对象
	 * @return json字符串
	 */
	fun toJsonStr(json: Any?): String {
		return gson.toJson(json)
	}

	/**
	 * 将json字符串转换成JsonObject
	 *
	 * @param json 需要转换的字符串对象
	 * @return JsonObject对象
	 */
	fun parseToJsonObject(json: String?): JsonObject {
		return JsonParser.parseString(json).asJsonObject
	}

	fun parseToJsonArray(json: String): JsonArray {
		return JsonParser.parseString(json).asJsonArray
	}
}

fun Any.toJsonObject(): JsonObject {
	return JsonUtil.gson.toJsonTree(this).asJsonObject
}