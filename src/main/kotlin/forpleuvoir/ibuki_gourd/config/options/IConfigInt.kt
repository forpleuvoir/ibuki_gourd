package forpleuvoir.ibuki_gourd.config.options

import forpleuvoir.ibuki_gourd.config.IConfigBaseValue

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 IConfigInt

 * 创建时间 2022/2/11 16:42

 * @author forpleuvoir

 */
interface IConfigInt : IConfigBaseValue<Int> {
	val minValue: Int
	val maxValue: Int
}