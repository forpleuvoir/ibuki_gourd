@file:Suppress("UNUSED")

package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.ibukigourd.text.inlinestyletext.InlineStyleTextParser
import moe.forpleuvoir.ibukigourd.text.style.StyleScope
import moe.forpleuvoir.nebula.common.color.RGBColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ComponentContents
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.contents.KeybindContents
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.network.chat.contents.TranslatableContents
import java.util.function.UnaryOperator
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
open class Text internal constructor(
    content: ComponentContents,
    siblings: MutableList<McText> = mutableListOf(),
    style: Style = Style.EMPTY
) : MutableComponent(content, siblings, style) {

    companion object {

        @JvmStatic
        fun inlineStyle(exp: String): Text {
            return InlineStyleTextParser.parse(exp).copyToText()
        }

        @JvmStatic
        fun literal(content: String): Text {
            return Text(PlainTextContents.create(content))
        }

        @JvmStatic
        fun empty(): Text {
            return Text(PlainTextContents.create(""))
        }

        @JvmStatic
        @JvmOverloads
        fun translatable(key: String, fallback: String? = null, vararg args: Any): Text {
            return Text(TranslatableContents(key, fallback, args))
        }

        @JvmStatic
        fun keyBind(translationKey: String): Text {
            return Text(KeybindContents(translationKey))
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
        return this.withStyle {
            StyleScope(it).apply(style).asStyle
        }
    }

    override fun setStyle(style: Style): Text {
        return super.setStyle(style) as Text
    }

    override fun withStyle(styleUpdater: UnaryOperator<Style>): Text {
        return super.withStyle(styleUpdater) as Text
    }

    override fun withStyle(styleOverride: Style): Text {
        return super.withStyle(styleOverride) as Text
    }

    override fun withStyle(vararg formattings: ChatFormatting): Text {
        return super.withStyle(*formattings) as Text
    }

    override fun withStyle(formatting: ChatFormatting): Text {
        return super.withStyle(formatting) as Text
    }

    fun withColor(rgbColor: RGBColor): Text {
        return super.withColor(rgbColor.rgb) as Text
    }

    fun withShadowColor(rgbColor: RGBColor): Text {
        return style { shadowColor(rgbColor) }
    }
}