package forpleuvoir.ibuki_gourd.config.options

import forpleuvoir.ibuki_gourd.config.IConfigBaseValue

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 IConfigMap

 * 创建时间 2022/2/11 16:44

 * @author forpleuvoir

 */
interface IConfigMap : IConfigBaseValue<Map<String, String>> {
	fun remove(key: String)
	fun rename(old: String, new: String)
	fun reset(oldKey: String, newKey: String, value: String)
	operator fun set(key: String, value: String)
	operator fun get(key: String): String?
}