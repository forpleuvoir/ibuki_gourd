@file:Suppress("UNUSED")

package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.ibukigourd.text.inline_style_text.InlineStyleTextParser
import moe.forpleuvoir.ibukigourd.text.style.StyleScope
import moe.forpleuvoir.nebula.common.color.RGBColor
import net.minecraft.text.*
import net.minecraft.util.Formatting
import java.util.function.UnaryOperator
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import net.minecraft.text.Text as McText

@OptIn(ExperimentalContracts::class)
open class Text(
    content: TextContent,
    siblings: MutableList<McText> = mutableListOf(),
    style: Style = Style.EMPTY
) : MutableText(content, siblings, style) {

    companion object {

        @JvmStatic
        fun inlineStyle(exp: String): Text {
            return InlineStyleTextParser.parse(exp).copyToText()
        }

        @JvmStatic
        fun literal(content: String): Text {
            return Text(PlainTextContent.Literal(content))
        }

        @JvmStatic
        fun empty(): Text {
            return Text(PlainTextContent.Literal(""))
        }

        @JvmStatic
        @JvmOverloads
        fun translatable(key: String, fallback: String? = null, vararg args: Any): Text {
            return Text(TranslatableTextContent(key, fallback, args))
        }

        @JvmStatic
        fun keyBind(translationKey: String): Text {
            return Text(KeybindTextContent(translationKey))
        }

    }

    val plainText: String get() = this.string

    fun appendNewLine(): Text {
        return this.appendLiteral("\n")
    }

    override fun append(text: McText): Text {
        siblings.add(text)
        return this
    }

    fun appendLiteral(text: String): Text {
        if (text.isEmpty()) {
            return this
        }
        return this.append(Literal(text))
    }

    fun appendTranslate(key: String, fallback: String? = null, vararg args: Any): Text {
        siblings.add(translatable(key, fallback, *args))
        return this
    }

    inline fun append(text: () -> McText): Text {
        contract {
            callsInPlace(text, InvocationKind.EXACTLY_ONCE)
        }
        siblings.add(text())
        return this
    }

    fun style(style: StyleScope.() -> Unit): Text {
        return this.styled {
            StyleScope(it).apply(style).asStyle
        }
    }

    override fun setStyle(style: Style): Text {
        return super.setStyle(style) as Text
    }

    override fun styled(styleUpdater: UnaryOperator<Style>): Text {
        return super.styled(styleUpdater) as Text
    }

    override fun fillStyle(styleOverride: Style?): Text {
        return super.fillStyle(styleOverride) as Text
    }

    override fun formatted(vararg formattings: Formatting): Text {
        return super.formatted(*formattings) as Text
    }

    override fun formatted(formatting: Formatting): Text {
        return super.formatted(formatting) as Text
    }

    fun withColor(rgbColor: RGBColor): Text {
        return super.withColor(rgbColor.rgb) as Text
    }

    fun withShadowColor(rgbColor: RGBColor): Text {
        return style { shadowColor(rgbColor) }
    }
}