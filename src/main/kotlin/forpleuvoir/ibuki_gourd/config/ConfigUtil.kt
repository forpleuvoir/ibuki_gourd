package forpleuvoir.ibuki_gourd.config

import com.google.gson.JsonObject
import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.utils.JsonUtil


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 ConfigUtil

 * 创建时间 2021/12/12 19:13

 * @author forpleuvoir

 */
object ConfigUtil {

	fun readConfigBase(root: JsonObject, category: String, options: List<ConfigBase>) {
		val obj = JsonUtil.getObject(root, category)
		if (obj != null){
			options.forEach {
				if(obj.has(it.name)){
					it.setValueFromJsonElement(obj[it.name])
				}
			}
		}
	}


}