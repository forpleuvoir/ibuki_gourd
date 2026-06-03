package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.ibukigourd.util.Size
import moe.forpleuvoir.ibukigourd.util.SizeFloat
import moe.forpleuvoir.nebula.common.util.primitive.either
import moe.forpleuvoir.nebula.common.util.primitive.sumOf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.FormattedCharSequence

@JvmName("textSize")
fun Iterable<FormattedText>.size(spacing: Float): SizeFloat {
    return Size(this.maxWidth, this.totalHeight(spacing))
}

fun Iterable<String>.size(spacing: Float): SizeFloat {
    return Size(this.maxWidth, this.totalHeight(spacing))
}

@JvmName("textTotalHeight")
fun Iterable<FormattedText>.totalHeight(spacing: Float): Float {
    return this.sumOf { it.height + spacing } - spacing
}

fun MutableComponent.totalHeight(spacing: Float, maxWidth: Float = 0f): Float {
    return this.wrapToTextLines(maxWidth).sumOf { it.height + spacing } - spacing
}

fun Component.totalHeight(spacing: Float, maxWidth: Float = 0f): Float {
    return this.wrapToTextLines(maxWidth).sumOf { it.height + spacing } - spacing
}

fun Iterable<String>.totalHeight(spacing: Float): Float {
    return this.sumOf { it.height + spacing } - spacing
}

fun String.totalHeight(spacing: Float, maxWidth: Float): Float {
    return this.wrapToLines(maxWidth).sumOf { it.height + spacing } - spacing
}

@get:JvmName("stringWidth")
val Iterable<String>.maxWidth: Float get() = this.maxOf { it.width }

val Iterable<FormattedText>.maxWidth: Float get() = this.maxOf { it.width }

@get:JvmName("orderedTextWidth")
val Iterable<FormattedCharSequence>.maxWidth: Float get() = this.maxOf { it.width }


/**
 * 此函数用于将含有特定字符的字符串分割成多行，然后将其添加到一个字符串列表中，并对被分割的每一部分进行额外的处理。
 * 额外的处理是通过`lineWrapping`函数参数指定的。
 *
 * @param width 行的宽度，默认值为0，表示不限制行宽，当行宽小于等于0时，只根据`needNewLine`参数决定何时换行.
 * @param needNewLine 指定哪些字符会触发换行，此参数表现为一个函数，接受一个字符，返回一个布尔值，真表示此字符会触发换行，默认为检查字符是否为换行符'\n'。
 * @param lineWrapping 当决定换行时，会调用此函数，此函数接受两个参数`start`和`end`，它们分别表示被分割部分在原始字符串中的开始和结束位置。
 *
 * @return 一个字符串列表，每个元素是原始字符串的一部分。
 *
 * ### 使用方法
 * ```kotlin
 * val str = "Hello\nWorld"
 * val lines = str.wrapToLines(textRenderer = myTextRenderer, width = 10, { it == '\n' }) { start, end ->
 *     // 这里的start和end代表的是"Hello"和"World"的在原文中的位置
 *     println("$start-$end")
 * }
 * println(lines) // 输出 ["Hello", "World"]
 * ```
 *
 * ### 注意事项
 * - 本函数只对源字符串进行读取操作，不会影响到源字符串。
 * - 在线程安全性方面，本函数由于依赖于`textRenderer`和`needNewLine`，可能会受到多线程环境下的影响。
 */
fun String.wrapToLines(
    width: Float = 0f,
    needNewLine: (Char) -> Boolean = { it == '\n' },
    lineWrapping: (start: Int, end: Int) -> Unit
): List<String> {
    val strings = mutableListOf<String>()
    var start = 0
    var end: Int
    val temp = StringBuilder()
    for (chr in this) {
        run {
            if (!needNewLine(chr)) {
                if (width <= 0) return@run
                if ((temp.toString() + chr).width <= width) return@run
            }
            strings.add(temp.toString())
            end = start + temp.length
            lineWrapping(start, end)
            start = (!needNewLine(chr)).either(end, end + 1)
            temp.clear()
        }
        if (!needNewLine(chr)) {
            temp.append(chr)
        }
    }
    strings.add(temp.toString())
    end = start + temp.length
    lineWrapping(start, end)
    return strings
}

/**
 * 该方法用于将字符串按照指定的最大宽度和条件封装为多行。
 *
 * @param maxWidth 表示一行文本的最大宽度，其默认值为0，表示无最大宽度限制。
 * @param needNewLine 定义一个函数，该函数决定哪个字符应该作为新行的开始，其默认行为是当遇到换行符('\n')时开始新的一行。
 *
 * 在开始时，该方法创建了两个对象：一个字符串列表(strings)，用于保存处理后的多行文本，另一个是临时的StringBuilder(temp)，用于存储当前正在处理的一行。
 * 通过对原字符串的每一个字符进行遍历，若当前字符不需要换行（通过needNewLine函数判断）并且添加当前字符后的临时字符串长度不超过maxWidth，该字符就被添加到临时字符串。
 * 若当前字符需要换行或者添加当前字符后的临时字符串长度超过maxWidth，当前的临时字符串就被添加到字符串列表并清空，用于存储下一行的文本。
 * 字符遍历结束后，将最后一次的临时字符串添加到字符串列表（因为在遍历期间，最后一行的文本可能未能添加到字符串列表）。
 *
 * 最后返回处理后的字符串列表。
 */
@JvmOverloads
fun String.wrapToLines(maxWidth: Float = 0f, needNewLine: (Char) -> Boolean = { it == '\n' }): List<String> {
    val strings = mutableListOf<String>()
    val temp = StringBuilder()
    for (chr in this) {
        run {
            if (!needNewLine(chr)) {
                if (maxWidth <= 0) return@run
                if ((temp.toString() + chr).width <= maxWidth) return@run
            }
            strings.add(temp.toString())
            temp.clear()
        }
        if (!needNewLine(chr)) {
            temp.append(chr)
        }
    }
    strings.add(temp.toString())
    return strings
}

/**
 * 该方法用于将字符串按照指定的最大宽度和条件封装为多行。
 * @see wrapToLines
 * @receiver [Collection]<[String]>
 * @param maxWidth Int
 * @param needNewLine Function1<Char, Boolean>
 * @return List<String>
 */
fun Iterable<String>.wrapToLines(
    maxWidth: Float = 0f,
    needNewLine: (Char) -> Boolean = { it == '\n' }
): List<String> {
    return buildList {
        for (text in this@wrapToLines) {
            addAll(text.wrapToLines(maxWidth, needNewLine))
        }
    }
}

/**
 * 对可变文本进行换行处理，将其转化为多段文本 (List<McText>)
 *
 * 这个函数将一个 [MutableComponent] 对象包装成一个 [Texts] 对象列表。每一个 [Texts] 对象
 * 代表一行文本。这个函数的制作思路是：逐个检查原文本的字符，如果当前字符需要换行（需要换行的条件由参数 `needNewLine` 决定），
 * 或者在保持原有文本排版的条件下，增加当前字符的宽度超过了参数 `maxWidth` 规定的最大宽度，这个函数就会切割这行文本，
 * 并创建一个新的 [Texts] 对象，继续处理余下的文本。
 *
 * @param maxWidth 每行文本的最大宽度，默认为 0，即不进行超过最大宽度时的换行处理
 * @param needNewLine 判断字符是否需要换新行的函数，默认为判断字符是否为 '\n'，
 * 若该函数判断结果为 true，则对应字符会被处理为新一行的开始
 * @return [List]<[Texts]> 返回多段文本的列表，其中每段文本表示一行的内容
 */
fun MutableComponent.wrapToTextLines(
    maxWidth: Float = 0f,
    needNewLine: (Char) -> Boolean = { it == '\n' }
): List<Text> {
    // 对当前可变文本进行扁平化处理，得到 McText 列表
    val flatList = this.flat()
    // 声明用于盛放处理后的Text对象的 list
    val texts = mutableListOf<MutableText>()
    // 声明用于构建每一行字符串的 StringBuilder
    val currentLineString = StringBuilder()
    // 迭代处理每一段文本
    flatList.forEach { text ->
        // 声明一个临时的StringBuilder用于存储临时字符
        val temp = StringBuilder()
        // 迭代处理每一段文本的每一个字符
        for (chr in text.string) {
            run {//检测是否换行的代码块
                //检查是否为换行符号
                if (!needNewLine(chr)) {  //不是换行符号
                    //检查最大宽度是否无限制
                    if (maxWidth <= 0f)
                        return@run //无限制宽度并且不是换行符号,所以跳出换行代码块,当次字符添加不换行
                    //不是换行符号,但是又宽度限制,检查添加到当前行的字符的长度
                    if ((currentLineString.toString() + chr).width <= maxWidth)
                        return@run  //小于等于最大宽度限制,跳出换行代码块,当次字符添加不换行
                }
                //没有跳出换行代码块,说明需要换行,所以需要将当前行的字符串添加到结果中,并创建新的一行
                if (texts.isNotEmpty()) {
                    //如果当前行不为空,并且temp不为空,说明当前行有内容,需要将temp的内容添加到结果中
                    if (temp.isNotEmpty()) texts.last().append(Literal(temp).setStyle(text.style))
                } else { //如果第一行为空说明当前是第一行,直接将temp添加到结果中
                    if (temp.isNotEmpty()) texts.add(Literal(temp).setStyle(text.style))
                }
                //添加完之后换行
                texts.add(Literal())
                //换行之后清空临时文本
                currentLineString.clear()
                temp.clear()
            }
            //执行到这说明没有换行,添加字符到当前行的字符串中
            if (!needNewLine(chr)) {
                temp.append(chr)
                currentLineString.append(chr)
            }
        }//当前的[text]处理完毕,并不代表当前行结束,需要将temp的内容添加到结果中

        if (texts.isNotEmpty()) {
            //如果当前行不为空,并且temp不为空,说明当前行有内容,需要将temp的内容添加到结果中
            if (temp.isNotEmpty()) texts.last().append(Literal(temp).setStyle(text.style))
        } else { //如果第一行为空说明当前是第一行,直接将temp添加到结果中
            if (temp.isNotEmpty()) texts.add(Literal(temp).setStyle(text.style))
        }
    }
    // 最后返回处理后的文本列表
    return texts
}

/**
 * 该函数是用于将[Texts]文本对象格式化到规定宽度的文本行中的工具函数
 *
 * 当[Texts]是[MutableComponent]类型时，直接调用[wrapToTextLines]来分行。
 * 如果不是，则先将其转换为字符串并按最大宽度`maxWidth`进行分行，
 * 然后将每一行文本转换为`Literal`类型并保留原来的样式。
 *
 * 作为结果的[Texts]对象列表中，每一个元素都代表着一个独立的文本行。
 *
 * @param maxWidth 单行文本的最大宽度, 默认为0, 表示无宽度限制
 * @return 返回格式化后的[Texts]对象列表
 */
fun Text.wrapToTextLines(maxWidth: Float = 0f): List<Text> {
    if (this is MutableComponent) {
        return this.wrapToTextLines(maxWidth)
    }
    return this.string
        .wrapToLines(maxWidth)
        .map { Literal(it).style { this.asStyle } }
}

fun Iterable<Text>.wrapToTextLines(maxWidth: Float = 0f): List<Text> {
    return buildList {
        for (text in this@wrapToTextLines) {
            addAll(text.wrapToTextLines(maxWidth))
        }
    }
}

fun Iterable<String>.wrapToSingle(maxWidth: Float = 0f): String {
    return buildString {
        this@wrapToSingle.forEachIndexed { index, text ->
            text.wrapToLines(maxWidth).let {
                it.forEachIndexed { i, t ->
                    append(t)
                    if (i != it.size - 1) append("\n")
                }
            }
            if (index != this@wrapToSingle.count() - 1) append("\n")
        }
    }
}

fun Iterable<Text>.wrapToSingleText(maxWidth: Float = 0f): Text {
    return Literal(buildString {
        this@wrapToSingleText.forEachIndexed { index, text ->
            text.wrapToTextLines(maxWidth).let {
                it.forEachIndexed { i, t ->
                    append(t.string)
                    if (i != it.size - 1) append("\n")
                }
            }
            if (index != this@wrapToSingleText.count() - 1) append("\n")
        }
    })
}
