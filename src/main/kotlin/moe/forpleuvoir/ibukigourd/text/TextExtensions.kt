@file:Suppress("UNUSED")

package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.nebula.common.pick
import net.minecraft.client.font.TextRenderer
import java.util.*
import moe.forpleuvoir.ibukigourd.util.textRenderer as tRenderer
import net.minecraft.text.Text as McText

@JvmOverloads
fun Literal(content: String = ""): Text = Text.literal(content)

@JvmOverloads
fun Trans(key: String, fallback: String? = null, vararg args: Any): Text = Text.translatable(key, fallback, *args)

fun Collection<String>.maxWidth(textRenderer: TextRenderer = tRenderer): Int {
    return this.maxOf { textRenderer.getWidth(it) }
}

@JvmName("maxTextWidth")
fun Collection<McText>.maxWidth(textRenderer: TextRenderer = tRenderer): Int {
    return this.maxOf { textRenderer.getWidth(it) }
}

fun String.wrapToLines(textRenderer: TextRenderer = tRenderer, width: Int = 0): List<String> {
    val texts: LinkedList<String> = LinkedList()
    var temp = StringBuilder()
    for (element in this) {
        run {
            if (element != '\n') {
                if (width <= 0) return@run
                if (textRenderer.getWidth(temp.toString() + element) <= width) return@run
            }
            texts.add(temp.toString())
            temp = StringBuilder()
        }
        if (element != '\n') {
            temp.append(element)
        }
    }
    texts.add(temp.toString())
    return texts
}

fun String.wrapToLines(textRenderer: TextRenderer = tRenderer, width: Int = 0, lineWrapping: (start: Int, end: Int) -> Unit): List<String> {
    val texts: LinkedList<String> = LinkedList()
    var start = 0
    var end: Int
    var temp = StringBuilder()
    for (chr in this) {
        run {
            if (chr != '\n') {
                if (width <= 0) return@run
                if (textRenderer.getWidth(temp.toString() + chr) <= width) return@run
            }
            texts.add(temp.toString())
            end = start + temp.length
            lineWrapping(start, end)
            start = (chr != '\n').pick(end, end + 1)
            temp = StringBuilder()
        }
        if (chr != '\n') {
            temp.append(chr)
        }
    }
    texts.add(temp.toString())
    end = start + temp.length
    lineWrapping(start, end)
    start = end
    return texts
}

fun Collection<String>.wrapToLines(textRenderer: TextRenderer = tRenderer, width: Int = 0): List<String> {
    return buildList {
        for (text in this@wrapToLines) {
            addAll(text.wrapToLines(textRenderer, width))
        }
    }
}

fun McText.wrapToTextLines(textRenderer: TextRenderer = tRenderer, width: Int = 0): List<McText> {
    return this.string
        .wrapToLines(textRenderer, width)
        .map { Literal(it).style { this.style } }
}

fun Collection<McText>.wrapToTextLines(textRenderer: TextRenderer = tRenderer, width: Int = 0): List<McText> {
    return buildList {
        for (text in this@wrapToTextLines) {
            addAll(text.wrapToTextLines(textRenderer, width))
        }
    }
}

fun List<String>.wrapToSingle(textRenderer: TextRenderer = tRenderer, width: Int = 0): String {
    return buildString {
        this@wrapToSingle.forEachIndexed { index, text ->
            text.wrapToLines(textRenderer, width).let {
                it.forEachIndexed { i, t ->
                    append(t)
                    if (i != it.size - 1) append("\n")
                }
            }
            if (index != this@wrapToSingle.size - 1) append("\n")
        }
    }
}

fun List<McText>.wrapToSingleText(textRenderer: TextRenderer = tRenderer, width: Int = 0): McText {
    return Literal(buildString {
        this@wrapToSingleText.forEachIndexed { index, text ->
            text.wrapToTextLines(textRenderer, width).let {
                it.forEachIndexed { i, t ->
                    append(t.string)
                    if (i != it.size - 1) append("\n")
                }
            }
            if (index != this@wrapToSingleText.size - 1) append("\n")
        }
    })
}
