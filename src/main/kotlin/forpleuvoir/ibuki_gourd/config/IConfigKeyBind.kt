package forpleuvoir.ibuki_gourd.config

import forpleuvoir.ibuki_gourd.keyboard.KeyEnvironment

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 IConfigKeyBind

 * 创建时间 2022/2/11 15:28

 * @author forpleuvoir

 */
interface IConfigKeyBind {
	fun initKeyBind()
	fun setKeyEnvironment(keyEnvironment: KeyEnvironment)
}