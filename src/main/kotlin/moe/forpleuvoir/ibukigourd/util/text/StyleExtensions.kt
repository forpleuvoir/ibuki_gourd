package moe.forpleuvoir.ibukigourd.util.text

import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.TextColor
import net.minecraft.util.Identifier

fun Style.withColor(color: Color): Style {
	return this.withColor(color.rgb)
}

fun style(
	color: Color? = null,
	bold: Boolean? = null,
	italic: Boolean? = null,
	underlined: Boolean? = null,
	strikethrough: Boolean? = null,
	obfuscated: Boolean? = null,
	clickEvent: ClickEvent? = null,
	hoverEvent: HoverEvent? = null,
	insertion: String? = null,
	font: Identifier? = null
): Style {
	return Style(color?.let { TextColor.fromRgb(it.rgb) }, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent, insertion, font)
}

