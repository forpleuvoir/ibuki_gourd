package forpleuvoir.ibuki_gourd.config.gui

import forpleuvoir.ibuki_gourd.common.tText
import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import net.minecraft.text.TranslatableText


/**
 * 配置组

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config.gui

 * 文件名 IConfigGroup

 * 创建时间 2021/12/21 15:32

 * @author forpleuvoir

 */
interface IConfigGroup {
	val key: String

	val displayKey: TranslatableText
		get() = key.tText()

	val remark: String

	val displayRemark: TranslatableText
		get() = remark.tText()

	val current: IConfigGroup

	fun all(): List<IConfigGroup>

	fun option(): List<ConfigBase>


	fun changeCurrent(configGroup: IConfigGroup)
}