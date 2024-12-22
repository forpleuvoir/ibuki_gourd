@file:Suppress("UNUSED", "FunctionName")

package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.nebula.event.Event
import moe.forpleuvoir.nebula.event.eventName
import moe.forpleuvoir.nebula.event.eventSimpleName
import net.minecraft.text.MutableText
import kotlin.reflect.KClass

typealias McText = net.minecraft.text.Text

@JvmOverloads
fun Literal(content: String = ""): Text = Text.literal(content)

fun Literal(content: StringBuilder): Text = Text.literal(content.toString())

@JvmOverloads
fun Translatable(key: String, fallback: String? = null, vararg args: Any): Text = Text.translatable(key, fallback, *args)

fun McText.copyToText(): Text = Text(content, ArrayList(siblings), style)

/**
 * 将MutableText进行扁平化处理.
 *
 * 这个函数将检查一个 [MutableText] 的每一个子元素，如果子元素也是一个 [MutableText]，则对其递归调用此操作。
 * 最后将处理过的文本以 [McText] 对象的形式添加到新的列表中。
 *
 * 使用 [Text.literal] 方法将 [MutableText] 对象的内容包装成一个 [McText] 对象，
 * 并通过 [styled] 方法将原样式应用到新的 [McText] 对象上，然后添加到新的列表中。
 *
 * 请注意，该方法将源自 [siblings] 的兄弟元素也做了处理，如果兄弟元素是 [MutableText]，
 * 则递归调用 [flat] 方法进行处理；如果不是，直接添加到新的列表中。
 *
 * @return 返回一个新的 [List]，包含了处理过的 [McText] 对象。
 */
fun MutableText.flat(): List<McText> {
    return buildList {
        add(MutableText.of(this@flat.content).setStyle(this@flat.style))
        this@flat.siblings.forEach { text ->
            if (text is MutableText) {
                addAll(text.flat())
            } else {
                add(text)
            }
        }
    }
}

val <T : Enum<T>> T.translateText: Text get() = Translatable("enum.${javaClass.name}.$name", name)

val <T : Enum<T>> T.translateComment: Text get() = Translatable("enum.${javaClass.name}.$name.comment", name)

val KClass<out Event>.translateText: Text get() = Translatable("event.$eventName", eventSimpleName)

val KClass<out Event>.translateComment: Text get() = Translatable("event.$eventName.comment", eventSimpleName)