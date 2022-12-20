package forpleuvoir.ibuki_gourd.utils

import net.minecraft.text.MutableText
import net.minecraft.text.Text

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.utils

 * 文件名 StringUtil

 * 创建时间 2021/12/17 16:23

 * @author forpleuvoir

 */

val String.text: Text
	get() = Text.of(this)


val String.mText: MutableText
	get() = Text.empty().append(this)

/**
 * 替换 & 字符为格式符号 § 的Text
 */
val String.fText: Text
	get() = Text.of(this.replace("&", "§"))
