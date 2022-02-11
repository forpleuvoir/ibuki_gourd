package forpleuvoir.ibuki_gourd.config.options

import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.config.IConfigToggle

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 IConfigBoolean

 * 创建时间 2022/2/11 16:27

 * @author forpleuvoir

 */
interface IConfigBoolean : IConfigBaseValue<Boolean>, IConfigToggle {
	override fun toggle() {
		this.setValue(!getValue())
	}
}