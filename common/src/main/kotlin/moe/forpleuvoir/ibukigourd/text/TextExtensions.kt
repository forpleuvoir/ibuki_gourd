@file:Suppress("UNUSED", "FunctionName")

package moe.forpleuvoir.ibukigourd.text

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.contents.TranslatableContents

typealias Text = Component

fun InlineStyleText(exp: String): MutableText = Texts.inlineStyle(exp)

@JvmOverloads
fun Literal(content: String = ""): MutableText = Text.literal(content)

fun Literal(content: StringBuilder): MutableText = Text.literal(content.toString())

@JvmOverloads
fun Translatable(key: String, fallback: String? = null, vararg args: Any): MutableText = MutableText.create(TranslatableContents(key, fallback, args))


/**
 * 将当前的可变文本对象转换为一个扁平化的、不可变的文本列表
 *
 * 此方法的主要目的是将一个可能包含多个子文本的复杂文本对象，
 * 转换为一个简单的文本列表，其中每个子文本都被视为独立的文本对象
 * 这对于文本处理和渲染非常有用，因为它允许统一处理所有子文本，
 * 而不必单独处理每个子文本的样式和内容
 *
 * @return 返回一个包含扁平化文本的列表，每个文本都是不可变的[Text]实例
 */
fun Text.flat(rootStyle: Style = Style.EMPTY): List<Text> = this.toFlatList(rootStyle)

val <T : Enum<T>> T.translateText: MutableText get() = Translatable("enum.${javaClass.name}.$name", name)

val <T : Enum<T>> T.translateComment: MutableText get() = Translatable("enum.${javaClass.name}.$name.comment", name)
