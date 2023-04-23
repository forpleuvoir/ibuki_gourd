@file:Suppress("UNUSED")

package com.forpleuvoir.ibukigourd.util.text

import net.minecraft.client.font.TextRenderer
import java.util.*
import com.forpleuvoir.ibukigourd.util.textRenderer as tRender

fun literal(content: String = ""): Text = Text.literal(content)

fun translatable(key: String): Text = Text.translatable(key)

fun serverText(key: String, fallback: String? = null, vararg args: Any): ServerText = ServerText(key, fallback, *args)

fun Collection<String>.maxWidth(textRenderer: TextRenderer = tRender): Int {
	var temp = 0
	for (s in this) {
		if (temp < textRenderer.getWidth(s))
			temp = textRenderer.getWidth(s)
	}
	return temp
}

@JvmName("maxTextWidth")
fun Collection<Text>.maxWidth(textRenderer: TextRenderer = tRender): Int {
	var temp = 0
	for (t in this) {
		if (temp < textRenderer.getWidth(t))
			temp = textRenderer.getWidth(t)
	}
	return temp
}

fun String.wrapToLines(textRenderer: TextRenderer = tRender, width: Int = 0): List<String> {
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

fun Collection<String>.wrapToLines(textRenderer: TextRenderer = tRender, width: Int = 0): List<String> {
	val texts: LinkedList<String> = LinkedList()
	for (text in this) {
		texts.addAll(text.wrapToLines(textRenderer, width))
	}
	return texts
}

fun Text.wrapToTextLines(
	textRenderer: TextRenderer = tRender,
	width: Int = 0
): List<Text> {
	val texts: LinkedList<Text> = LinkedList()
	this.string.wrapToLines(textRenderer, width).forEach { texts.add(literal(it)) }
	return texts
}

fun Collection<Text>.wrapToTextLines(
	textRenderer: TextRenderer = tRender,
	width: Int = 0
): List<Text> {
	val texts: LinkedList<Text> = LinkedList()
	for (text in this) {
		texts.addAll(text.wrapToTextLines(textRenderer, width))
	}
	return texts
}

fun List<String>.wrapToSingle(textRenderer: TextRenderer = tRender, width: Int = 0): String {
	val sb = StringBuilder()
	this.forEachIndexed { index, text ->
		val wrapToLines = text.wrapToLines(textRenderer, width)
		wrapToLines.forEachIndexed { i, t ->
			sb.append(t)
			if (i != wrapToLines.size - 1) sb.append("\n")
		}
		if (index != this.size - 1) sb.append("\n")
	}
	return sb.toString()
}

fun List<Text>.wrapToSingleText(
	textRenderer: TextRenderer = tRender,
	width: Int = 0
): Text {
	val sb = StringBuilder()
	this.forEachIndexed { index, text ->
		val wrapToLines = text.wrapToTextLines(textRenderer, width)
		wrapToLines.forEachIndexed { i, t ->
			sb.append(t.string)
			if (i != wrapToLines.size - 1) sb.append("\n")
		}
		if (index != this.size - 1) sb.append("\n")
	}
	return literal(sb.toString())
}
