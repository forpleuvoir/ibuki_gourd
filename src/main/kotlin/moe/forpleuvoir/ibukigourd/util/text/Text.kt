@file:Suppress("UNUSED")

package moe.forpleuvoir.ibukigourd.util.text

import moe.forpleuvoir.ibukigourd.input.KeyCode
import net.minecraft.text.*
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.function.UnaryOperator
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
open class Text(
    content: TextContent,
    siblings: List<Text> = emptyList(),
    style: Style = Style.EMPTY
) : MutableText(content, siblings, style) {

    companion object {

        @JvmStatic
        fun literal(content: String): moe.forpleuvoir.ibukigourd.util.text.Text {
            return Text(PlainTextContent.Literal(content))
        }

        @JvmStatic
        fun empty(): moe.forpleuvoir.ibukigourd.util.text.Text {
            return Text(PlainTextContent.Literal(""))
        }

        @JvmStatic
        @JvmOverloads
        fun translatable(key: String, fallback: String? = null, vararg args: Any): moe.forpleuvoir.ibukigourd.util.text.Text {
            return Text(TranslatableTextContent(key, fallback, args))
        }

        @JvmStatic
        fun keyBind(keyCode: KeyCode): moe.forpleuvoir.ibukigourd.util.text.Text {
            return Text(KeybindTextContent(keyCode.translationKey))
        }

    }

    val plainText: String get() = this.string

    override fun append(text: Text): moe.forpleuvoir.ibukigourd.util.text.Text {
        siblings.add(text)
        return this
    }

    fun appendLiteral(text: String): moe.forpleuvoir.ibukigourd.util.text.Text {
        siblings.add(literal(text))
        return this
    }

    fun appendTranslate(key: String, fallback: String? = null, vararg args: Any): moe.forpleuvoir.ibukigourd.util.text.Text {
        siblings.add(translatable(key, fallback, *args))
        return this
    }

    inline fun append(text: () -> Text): moe.forpleuvoir.ibukigourd.util.text.Text {
        contract {
            callsInPlace(text, InvocationKind.EXACTLY_ONCE)
        }
        siblings.add(text())
        return this
    }

    override fun setStyle(style: Style): moe.forpleuvoir.ibukigourd.util.text.Text {
        return super.setStyle(style) as moe.forpleuvoir.ibukigourd.util.text.Text
    }


    inline fun style(style: (Style) -> Style): moe.forpleuvoir.ibukigourd.util.text.Text {
        contract {
            callsInPlace(style, InvocationKind.EXACTLY_ONCE)
        }
        this.style = style(this.style)
        return this
    }

    override fun styled(styleUpdater: UnaryOperator<Style>): moe.forpleuvoir.ibukigourd.util.text.Text {
        return super.styled(styleUpdater) as moe.forpleuvoir.ibukigourd.util.text.Text
    }

    override fun fillStyle(styleOverride: Style?): moe.forpleuvoir.ibukigourd.util.text.Text {
        return super.fillStyle(styleOverride) as moe.forpleuvoir.ibukigourd.util.text.Text
    }

    override fun formatted(vararg formattings: Formatting): moe.forpleuvoir.ibukigourd.util.text.Text {
        return super.formatted(*formattings) as moe.forpleuvoir.ibukigourd.util.text.Text
    }

    override fun formatted(formatting: Formatting): moe.forpleuvoir.ibukigourd.util.text.Text {
        return super.formatted(formatting) as moe.forpleuvoir.ibukigourd.util.text.Text
    }
}