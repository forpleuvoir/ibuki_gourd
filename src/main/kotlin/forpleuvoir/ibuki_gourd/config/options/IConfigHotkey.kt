package forpleuvoir.ibuki_gourd.config.options

import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.config.IConfigKeyBind
import forpleuvoir.ibuki_gourd.keyboard.KeyBind

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 IConfigHotkey

 * 创建时间 2022/2/11 17:06

 * @author forpleuvoir

 */
interface IConfigHotkey : IConfigKeyBind, IConfigBaseValue<KeyBind> {
	fun setKeyCallback(callback: () -> Unit)
}