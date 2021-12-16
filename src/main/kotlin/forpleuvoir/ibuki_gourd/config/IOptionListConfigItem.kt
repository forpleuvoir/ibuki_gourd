package forpleuvoir.ibuki_gourd.config

import forpleuvoir.ibuki_gourd.common.tText
import net.minecraft.text.TranslatableText


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 OptionListConfigItem

 * 创建时间 2021/12/12 14:56

 * @author forpleuvoir

 */
interface IOptionListConfigItem {

	val name: String

	val displayName: TranslatableText
		get() = name.tText()

	val remark: String

	val displayRemark: TranslatableText
		get() = remark.tText()

	fun cycle(): IOptionListConfigItem

	fun fromString(string: String): IOptionListConfigItem

	fun getAllItem(): List<IOptionListConfigItem>

}