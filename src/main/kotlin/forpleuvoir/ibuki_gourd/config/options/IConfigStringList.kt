package forpleuvoir.ibuki_gourd.config.options

import forpleuvoir.ibuki_gourd.config.IConfigBaseValue

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 IConfigStringList

 * 创建时间 2022/2/11 16:47

 * @author forpleuvoir

 */
interface IConfigStringList : IConfigBaseValue<List<String>> {
	fun add(string: String)
	fun set(index: Int, string: String)
	fun remove(index: Int)
	fun remove(string: String)
}