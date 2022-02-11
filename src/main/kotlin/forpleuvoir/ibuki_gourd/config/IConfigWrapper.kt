package forpleuvoir.ibuki_gourd.config

import forpleuvoir.ibuki_gourd.config.gui.ConfigWrapper


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 IConfigWrapper

 * 创建时间 2021/12/26 17:15

 * @author forpleuvoir

 */
interface IConfigWrapper {
	fun wrapper(x: Int = 0, y: Int = 0, width: Int = 140, height: Int = 20): ConfigWrapper
}