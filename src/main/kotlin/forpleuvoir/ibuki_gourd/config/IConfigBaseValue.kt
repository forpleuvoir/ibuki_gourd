package forpleuvoir.ibuki_gourd.config


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 IConfigBaseValue

 * 创建时间 2021/12/9 18:30

 * @author forpleuvoir

 */
interface IConfigBaseValue<T> {

	val defaultValue: T
	fun getValue(): T
	fun setValue(value: T)
	fun isEquals(value: Any?): Boolean {
		return value == getValue()
	}

}