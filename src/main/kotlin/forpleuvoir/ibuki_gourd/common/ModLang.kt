package forpleuvoir.ibuki_gourd.common

import net.minecraft.client.resource.language.I18n
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TextContent
import net.minecraft.text.TranslatableTextContent


/**
 * 语言

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.common

 * 文件名 ModLang

 * 创建时间 2021/12/9 16:10

 * @author forpleuvoir

 */
interface ModLang {
	val key: String
	val modId: String

	fun tText(vararg args: Any?): MutableText {
		return MutableText.of(TranslatableTextContent("$modId.$key", null, args))
	}

	fun tString(vararg args: Any?): String {
		return I18n.translate("$modId.$key", *args)
	}
}

fun String.tText(vararg args: Any?): TranslatableTextContent {
	return TranslatableTextContent(this, null, args)
}

val TextContent.text: Text get() = MutableText.of(this)

val TextContent.mText: MutableText get() = MutableText.of(this)

fun String.tString(vararg args: Any?): String {
	return I18n.translate(this, *args)
}