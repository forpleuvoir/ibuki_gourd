@file:Suppress("UNUSED")

package com.forpleuvoir.ibukigourd.util.text

import net.minecraft.text.*
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.function.UnaryOperator

open class Text(
	content: TextContent,
	siblings: List<Text> = emptyList(),
	style: Style = Style.EMPTY
) : MutableText(content, siblings, style) {

	companion object {

		@JvmStatic
		fun literal(content: String): com.forpleuvoir.ibukigourd.util.text.Text {
			return Text(LiteralTextContent(content))
		}

		@JvmStatic
		fun empty(): com.forpleuvoir.ibukigourd.util.text.Text {
			return Text(LiteralTextContent(""))
		}

		@JvmStatic
		fun translatable(key: String): com.forpleuvoir.ibukigourd.util.text.Text {
			return Text(TranslatableTextContent(key, null, emptyArray()))
		}

	}

	val plainText: String get() = this.string

	override fun append(text: Text): com.forpleuvoir.ibukigourd.util.text.Text {
		siblings.add(text)
		return this
	}

	fun appendLiteral(text: String): com.forpleuvoir.ibukigourd.util.text.Text {
		siblings.add(literal(text))
		return this
	}

	fun appendTranslate(text: String): com.forpleuvoir.ibukigourd.util.text.Text {
		siblings.add(translatable(text))
		return this
	}

	inline fun append(text: () -> Text): com.forpleuvoir.ibukigourd.util.text.Text {
		siblings.add(text())
		return this
	}

	override fun setStyle(style: Style): com.forpleuvoir.ibukigourd.util.text.Text {
		return super.setStyle(style) as com.forpleuvoir.ibukigourd.util.text.Text
	}

	inline fun style(style: (Style) -> Style): com.forpleuvoir.ibukigourd.util.text.Text {
		this.style = style(this.style)
		return this
	}

	override fun styled(styleUpdater: UnaryOperator<Style>): com.forpleuvoir.ibukigourd.util.text.Text {
		return super.styled(styleUpdater) as com.forpleuvoir.ibukigourd.util.text.Text
	}

	override fun fillStyle(styleOverride: Style?): com.forpleuvoir.ibukigourd.util.text.Text {
		return super.fillStyle(styleOverride) as com.forpleuvoir.ibukigourd.util.text.Text
	}

	override fun formatted(vararg formattings: Formatting): com.forpleuvoir.ibukigourd.util.text.Text {
		return super.formatted(*formattings) as com.forpleuvoir.ibukigourd.util.text.Text
	}

	override fun formatted(formatting: Formatting): com.forpleuvoir.ibukigourd.util.text.Text {
		return super.formatted(formatting) as com.forpleuvoir.ibukigourd.util.text.Text
	}
}