package forpleuvoir.ibuki_gourd.config


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 IConfigGroup

 * 创建时间 2021/12/12 16:31

 * @author forpleuvoir

 */
interface IConfigGroup<T> {

	fun getValueItem(key: String): T?

	fun getKeys(): Set<String>

	fun containsKey(key: String): Boolean = getKeys().contains(key)
}