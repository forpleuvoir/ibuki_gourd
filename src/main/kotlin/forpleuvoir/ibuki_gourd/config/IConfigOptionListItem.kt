package forpleuvoir.ibuki_gourd.config

import forpleuvoir.ibuki_gourd.common.tText
import net.minecraft.text.TranslatableText


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 IConfigOptionListItem

 * 创建时间 2021/12/12 14:56

 * @author forpleuvoir

 */
interface IConfigOptionListItem {

	val key: String

	val displayName: TranslatableText
		get() = key.tText()

	val remark: String

	val displayRemark: TranslatableText
		get() = remark.tText()

	fun cycle(): IConfigOptionListItem

	fun fromString(string: String): IConfigOptionListItem

	fun getAllItem(): List<IConfigOptionListItem>

}