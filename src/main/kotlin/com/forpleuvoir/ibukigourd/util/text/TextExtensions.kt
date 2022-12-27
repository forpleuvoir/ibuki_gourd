@file:Suppress("UNUSED")

package com.forpleuvoir.ibukigourd.util.text

import com.forpleuvoir.ibukigourd.util.textRenderer
import java.util.*

fun literal(content: String = ""): Text = Text.literal(content)

fun translate(key: String): Text = Text.translate(key)

fun serverText(key: String, vararg args: Any): ServerText = ServerText(key, *args)

fun Collection<String>.maxWidth(): Int {
	var temp = 0
	for (s in this) {
		if (temp < textRenderer.getWidth(s))
			temp = textRenderer.getWidth(s)
	}
	return temp
}

fun Collection<net.minecraft.text.Text>.maxWidth(): Int {
	var temp = 0
	for (t in this) {
		if (temp < textRenderer.getWidth(t))
			temp = textRenderer.getWidth(t)
	}
	return temp
}

fun String.wrapToLines(width: Int = 0): List<String> {
	val texts: LinkedList<String> = LinkedList()
	var temp = StringBuilder()
	for (element in this) {
		run {
			if (element != '\n') {
				if (width == 0) return@run
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

fun Collection<String>.wrapToLines(width: Int = 0): List<String> {
	val texts: LinkedList<String> = LinkedList()
	for (text in this) {
		texts.addAll(text.wrapToLines(width))
	}
	return texts
}

fun net.minecraft.text.Text.wrapToLines(width: Int = 0): List<net.minecraft.text.Text> {
	val texts: LinkedList<net.minecraft.text.Text> = LinkedList()
	this.string.wrapToLines(width).forEach { texts.add(literal(it)) }
	return texts
}

fun Collection<net.minecraft.text.Text>.wrapToLines(width: Int = 0): List<net.minecraft.text.Text> {
	val texts: LinkedList<net.minecraft.text.Text> = LinkedList()
	for (text in this) {
		texts.addAll(text.wrapToLines(width))
	}
	return texts
}

fun List<String>.wrapToSingleText(width: Int = 0): String {
	val sb = StringBuilder()
	this.forEachIndexed { index, text ->
		val wrapToLines = text.wrapToLines(width)
		wrapToLines.forEachIndexed { i, t ->
			sb.append(t)
			if (i != wrapToLines.size - 1) sb.append("\n")
		}
		if (index != this.size - 1) sb.append("\n")
	}
	return sb.toString()
}

fun List<net.minecraft.text.Text>.wrapToSingleText(width: Int = 0): net.minecraft.text.Text {
	val sb = StringBuilder()
	this.forEachIndexed { index, text ->
		val wrapToLines = text.wrapToLines(width)
		wrapToLines.forEachIndexed { i, t ->
			sb.append(t.string)
			if (i != wrapToLines.size - 1) sb.append("\n")
		}
		if (index != this.size - 1) sb.append("\n")
	}
	return literal(sb.toString())
}
