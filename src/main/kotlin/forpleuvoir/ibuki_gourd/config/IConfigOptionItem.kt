package forpleuvoir.ibuki_gourd.config

import forpleuvoir.ibuki_gourd.common.tText
import net.minecraft.text.TranslatableText


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 IConfigOptionItem

 * 创建时间 2021/12/12 14:56

 * @author forpleuvoir

 */
interface IConfigOptionItem {

	val key: String

	val displayKey: TranslatableText
		get() = key.tText()

	val remark: String

	val displayRemark: TranslatableText
		get() = remark.tText()

	fun cycle(): IConfigOptionItem

	fun fromString(string: String): IConfigOptionItem

	fun getAllItem(): List<IConfigOptionItem>

}