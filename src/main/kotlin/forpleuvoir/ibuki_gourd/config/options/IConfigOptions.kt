package forpleuvoir.ibuki_gourd.config.options

import forpleuvoir.ibuki_gourd.config.IConfigBaseValue
import forpleuvoir.ibuki_gourd.config.IConfigOptionItem
import forpleuvoir.ibuki_gourd.config.IConfigToggle

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.options

 * 文件名 IConfigOptions

 * 创建时间 2022/2/11 18:45

 * @author forpleuvoir

 */
interface IConfigOptions : IConfigBaseValue<IConfigOptionItem>, IConfigToggle {
	override fun toggle() {
		this.setValue(this.getValue().cycle())
	}
}