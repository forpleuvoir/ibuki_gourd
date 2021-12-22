package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.gui.screen.IScreenTabEntry


/**
 * 配置组

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 IConfigGroup

 * 创建时间 2021/12/21 15:32

 * @author forpleuvoir

 */
interface IConfigGroup : IScreenTabEntry {
	val configs: List<ConfigBase>
}