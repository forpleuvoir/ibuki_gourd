package forpleuvoir.ibuki_gourd.config.options

import forpleuvoir.ibuki_gourd.config.IConfigBaseValue


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 IConfigDouble

 * 创建时间 2022/2/11 16:36

 * @author forpleuvoir

 */
interface IConfigDouble : IConfigBaseValue<Double> {
	val minValue: Double
	val maxValue: Double
}