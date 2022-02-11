package forpleuvoir.ibuki_gourd.config.options

import forpleuvoir.ibuki_gourd.config.IConfigBase
import forpleuvoir.ibuki_gourd.config.IConfigBaseValue


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 IConfigGroup

 * 创建时间 2021/12/12 16:31

 * @author forpleuvoir

 */
interface IConfigGroup : IConfigBaseValue<List<IConfigBase>> {

	fun getValueItem(key: String): IConfigBase?

	fun getKeys(): Set<String>

	fun containsKey(key: String): Boolean = getKeys().contains(key)
}