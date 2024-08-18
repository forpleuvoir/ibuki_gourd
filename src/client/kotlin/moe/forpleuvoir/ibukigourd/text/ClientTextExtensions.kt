package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.ibukigourd.gui.base.render.Size
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeFloat
import moe.forpleuvoir.ibukigourd.gui.base.render.SizeInt
import moe.forpleuvoir.nebula.common.util.primitive.pick
import net.minecraft.client.font.TextRenderer
import net.minecraft.text.MutableText
import moe.forpleuvoir.ibukigourd.util.textRenderer as tRenderer

fun McText.size(textRenderer: TextRenderer = tRenderer): SizeInt {
    return Size(textRenderer.getWidth(this), textRenderer.fontHeight)
}

fun String.size(textRenderer: TextRenderer = tRenderer): SizeInt {
    return Size(textRenderer.getWidth(this), textRenderer.fontHeight)
}

fun Collection<McText>.size(textRenderer: TextRenderer = tRenderer, spacing: Float): SizeFloat {
    return Size(this.maxWidth(textRenderer).toFloat(), this.size * (textRenderer.fontHeight + spacing) - spacing)
}

fun Collection<String>.size(textRenderer: TextRenderer = tRenderer, spacing: Float): SizeFloat {
    return Size(this.maxWidth(textRenderer).toFloat(), this.size * (textRenderer.fontHeight + spacing) - spacing)
}

@JvmName("textTotalHeight")
fun Collection<McText>.totalHeight(textRenderer: TextRenderer = tRenderer, spacing: Float): Float {
    return this.size * (textRenderer.fontHeight + spacing) - spacing
}

fun McText.totalHeight(textRenderer: TextRenderer = tRenderer, spacing: Float, maxWidth: Int): Float {
    return this.wrapToTextLines(textRenderer, maxWidth).size * (textRenderer.fontHeight + spacing) - spacing
}

fun Collection<String>.totalHeight(textRenderer: TextRenderer = tRenderer, spacing: Float): Float {
    return this.size * (textRenderer.fontHeight + spacing) - spacing
}

fun String.totalHeight(textRenderer: TextRenderer = tRenderer, spacing: Float, maxWidth: Int): Float {
    return this.wrapToLines(textRenderer, maxWidth).size * (textRenderer.fontHeight + spacing) - spacing
}

/**
 * 获取当前[String]集合中的最大宽度
 * @receiver [Collection]<[String]>
 * @param textRenderer [TextRenderer]
 * @return Int
 */
fun Collection<String>.maxWidth(textRenderer: TextRenderer = tRenderer): Int {
    return this.maxOf { textRenderer.getWidth(it) }
}


/**
 * 获取当前[McText]集合中的最大宽度
 * @receiver [Collection]<[McText]>
 * @param textRenderer [TextRenderer]
 * @return Int
 */
@JvmName("maxTextWidth")
fun Collection<McText>.maxWidth(textRenderer: TextRenderer = tRenderer): Int {
    return this.maxOf { textRenderer.getWidth(it) }
}

/**
 * 此函数用于将含有特定字符的字符串分割成多行，然后将其添加到一个字符串列表中，并对被分割的每一部分进行额外的处理。
 * 额外的处理是通过`lineWrapping`函数参数指定的。
 *
 * @param textRenderer 默认的文字渲染器，通过计算字符宽度来决定何时换行.
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
    textRenderer: TextRenderer = tRenderer,
    width: Int = 0,
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
                if (textRenderer.getWidth(temp.toString() + chr) <= width) return@run
            }
            strings.add(temp.toString())
            end = start + temp.length
            lineWrapping(start, end)
            start = (!needNewLine(chr)).pick(end, end + 1)
            temp.clear()
        }
        if (!needNewLine(chr)) {
            temp.append(chr)
        }
    }
    strings.add(temp.toString())
    end = start + temp.length
    lineWrapping(start, end)
    start = end
    return strings
}

/**
 * 该方法用于将字符串按照指定的最大宽度和条件封装为多行。
 *
 * @param textRenderer 定义文本渲染器，用于计算文本的宽度，其默认值为[tRenderer]。
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
fun String.wrapToLines(textRenderer: TextRenderer = tRenderer, maxWidth: Int = 0, needNewLine: (Char) -> Boolean = { it == '\n' }): List<String> {
    val strings = mutableListOf<String>()
    val temp = StringBuilder()
    for (chr in this) {
        run {
            if (!needNewLine(chr)) {
                if (maxWidth <= 0) return@run
                if (textRenderer.getWidth(temp.toString() + chr) <= maxWidth) return@run
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
 * @param textRenderer TextRenderer
 * @param maxWidth Int
 * @param needNewLine Function1<Char, Boolean>
 * @return List<String>
 */
fun Collection<String>.wrapToLines(
    textRenderer: TextRenderer = tRenderer,
    maxWidth: Int = 0,
    needNewLine: (Char) -> Boolean = { it == '\n' }
): List<String> {
    return buildList {
        for (text in this@wrapToLines) {
            addAll(text.wrapToLines(textRenderer, maxWidth, needNewLine))
        }
    }
}

/**
 * 对可变文本进行换行处理，将其转化为多段文本 (List<McText>)
 *
 * 这个函数将一个 [MutableText] 对象包装成一个 [McText] 对象列表。每一个 [McText] 对象
 * 代表一行文本。这个函数的制作思路是：逐个检查原文本的字符，如果当前字符需要换行（需要换行的条件由参数 `needNewLine` 决定），
 * 或者在保持原有文本排版的条件下，增加当前字符的宽度超过了参数 `maxWidth` 规定的最大宽度，这个函数就会切割这行文本，
 * 并创建一个新的 [McText] 对象，继续处理余下的文本。
 *
 * @param textRenderer 文本渲染器，默认为 tRenderer
 * @param maxWidth 每行文本的最大宽度，默认为 0，即不进行超过最大宽度时的换行处理
 * @param needNewLine 判断字符是否需要换新行的函数，默认为判断字符是否为 '\n'，
 * 若该函数判断结果为 true，则对应字符会被处理为新一行的开始
 * @return [List]<[McText]> 返回多段文本的列表，其中每段文本表示一行的内容
 */
fun MutableText.wrapToTextLines(
    textRenderer: TextRenderer = tRenderer,
    maxWidth: Int = 0,
    needNewLine: (Char) -> Boolean = { it == '\n' }
): List<McText> {
    // 对当前可变文本进行扁平化处理，得到 McText 列表
    val flatList = this.flat()
    // 声明用于盛放处理后的Text对象的 list
    val texts = mutableListOf<Text>()
    // 声明用于构建每一行字符串的 StringBuilder
    val currentLineString = StringBuilder()
    // 迭代处理每一段文本
    var newline = false
    flatList.forEach { text ->
        // 声明一个临时的StringBuilder用于存储临时字符
        val temp = StringBuilder()
        // 迭代处理每一段文本的每一个字符
        for (chr in text.string) {
            run {
                // 如果字符不需要换行，且不超过最大宽度，则追加到当前行字符串后面
                if (!needNewLine(chr)) {
                    if (maxWidth <= 0) return@run
                    if (textRenderer.getWidth(currentLineString.toString() + chr) <= maxWidth) return@run
                }
                // 否则，将临时字符串添加到文本列表中，然后清空临时字符串及当前行字符串
                texts.add(Literal(temp).setStyle(text.style))
                newline = true
                temp.clear()
                currentLineString.clear()
            }
            // 如果字符不需要换行，则追加到临时字符串及当前行字符串后面
            if (!needNewLine(chr)) {
                temp.append(chr)
                currentLineString.append(chr)
            }
        }
        if (texts.isNotEmpty() && !newline) {
            texts.last().append(Literal(temp).setStyle(text.style))
        } else {
            texts.add(Literal(temp).setStyle(text.style))
            newline = false
        }
    }
    // 最后返回处理后的文本列表
    return texts
}

/**
 * 该函数是用于将[McText]文本对象格式化到规定宽度的文本行中的工具函数
 *
 * 当[McText]是[MutableText]类型时，直接调用[wrapToTextLines]来分行。
 * 如果不是，则先将其转换为字符串并按最大宽度`maxWidth`进行分行，
 * 然后将每一行文本转换为`Literal`类型并保留原来的样式。
 *
 * 作为结果的[McText]对象列表中，每一个元素都代表着一个独立的文本行。
 *
 * @param textRenderer 用于文本渲染的渲染器, 默认值为[tRenderer]
 * @param maxWidth 单行文本的最大宽度, 默认为0, 表示无宽度限制
 * @return 返回格式化后的[McText]对象列表
 */
fun McText.wrapToTextLines(textRenderer: TextRenderer = tRenderer, maxWidth: Int = 0): List<McText> {
    if (this is MutableText) {
        return this.wrapToTextLines(textRenderer, maxWidth)
    }
    return this.string
        .wrapToLines(textRenderer, maxWidth)
        .map { Literal(it).style { this.style } }
}

fun Collection<McText>.wrapToTextLines(textRenderer: TextRenderer = tRenderer, maxWidth: Int = 0): List<McText> {
    return buildList {
        for (text in this@wrapToTextLines) {
            addAll(text.wrapToTextLines(textRenderer, maxWidth))
        }
    }
}

fun List<String>.wrapToSingle(textRenderer: TextRenderer = tRenderer, maxWidth: Int = 0): String {
    return buildString {
        this@wrapToSingle.forEachIndexed { index, text ->
            text.wrapToLines(textRenderer, maxWidth).let {
                it.forEachIndexed { i, t ->
                    append(t)
                    if (i != it.size - 1) append("\n")
                }
            }
            if (index != this@wrapToSingle.size - 1) append("\n")
        }
    }
}

fun List<McText>.wrapToSingleText(textRenderer: TextRenderer = tRenderer, maxWidth: Int = 0): McText {
    return Literal(buildString {
        this@wrapToSingleText.forEachIndexed { index, text ->
            text.wrapToTextLines(textRenderer, maxWidth).let {
                it.forEachIndexed { i, t ->
                    append(t.string)
                    if (i != it.size - 1) append("\n")
                }
            }
            if (index != this@wrapToSingleText.size - 1) append("\n")
        }
    })
}
