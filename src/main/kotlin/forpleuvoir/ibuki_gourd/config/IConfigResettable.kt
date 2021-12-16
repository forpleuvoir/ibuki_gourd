package forpleuvoir.ibuki_gourd.config


/**
 * 可重置配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 IConfigResettable

 * 创建时间 2021/12/9 18:01

 * @author forpleuvoir

 */
interface IConfigResettable {

	/**
	 * 是否为默认值
	 */
	val isDefaultValue: Boolean

	/**
	 * 重置为默认值
	 */
	fun resetDefaultValue()

}