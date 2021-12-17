package forpleuvoir.ibuki_gourd.utils.color

import forpleuvoir.ibuki_gourd.common.IJsonData


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.utils.color

 * 文件名 Color

 * 创建时间 2021/12/13 18:55

 * @author forpleuvoir

 */
interface IColor<T> : IJsonData {
	val intValue: Int
	val hexString: String
		get() = intValue.toString(16)

	fun intValue(alpha: T): Int
	fun fromInt(color: Int): IColor<T>

}