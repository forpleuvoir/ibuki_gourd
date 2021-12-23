package forpleuvoir.ibuki_gourd.utils



/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.utils

 * 文件名 NumberUtil

 * 创建时间 2021/12/23 21:43

 * @author forpleuvoir

 */
fun Number.clamp(minValue: Number, maxValue: Number): Number {
	if (this.toDouble() < minValue.toDouble()) {
		return minValue
	}
	return if (this.toDouble() > maxValue.toDouble()) {
		maxValue
	} else this
}
