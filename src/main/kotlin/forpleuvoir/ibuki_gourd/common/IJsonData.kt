package forpleuvoir.ibuki_gourd.common

import com.google.gson.JsonElement


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.common

 * 文件名 IJsonData

 * 创建时间 2021/12/9 17:54

 * @author forpleuvoir

 */
interface IJsonData {
	/**
	 * 从JsonElement中获取数据
	 * @param jsonElement JsonElement
	 */
	fun setValueFromJsonElement(jsonElement: JsonElement)

	/**
	 * 转换为JsonElement
	 * @return JsonElement
	 */
	val asJsonElement: JsonElement

}