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
	val rgb: Int
	val hexString: String
		get() = "0x${rgb.toString(16).uppercase()}"

	fun rgb(alpha: T): Int
	fun fromInt(color: Int): IColor<T>
	fun alpha(alpha: T): IColor<T> {
		return fromInt(this.rgb(alpha))
	}

	var red: T
	var green: T
	var blue: T
	var alpha: T

}