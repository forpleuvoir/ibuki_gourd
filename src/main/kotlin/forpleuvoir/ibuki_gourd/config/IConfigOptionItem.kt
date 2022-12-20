package forpleuvoir.ibuki_gourd.config

import forpleuvoir.ibuki_gourd.common.mText
import forpleuvoir.ibuki_gourd.common.tText
import net.minecraft.text.MutableText


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

	val displayKey: MutableText
		get() = key.tText().mText

	val remark: String

	val displayRemark: MutableText
		get() = remark.tText().mText

	fun cycle(): IConfigOptionItem

	fun fromKey(key: String): IConfigOptionItem

	fun getAllItem(): List<IConfigOptionItem>

}