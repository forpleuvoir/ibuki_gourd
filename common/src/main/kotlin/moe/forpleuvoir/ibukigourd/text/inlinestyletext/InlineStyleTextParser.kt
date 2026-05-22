package moe.forpleuvoir.ibukigourd.text.inlinestyletext

import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.Texts
import moe.forpleuvoir.ibukigourd.text.flat
import moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier.*
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.util.primitive.either
import moe.forpleuvoir.nebula.common.util.primitive.fillBefore
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.KeybindContents
import net.minecraft.network.chat.contents.TranslatableContents

object InlineStyleTextParser {

    val allModifier = arrayOf(
        TextContentModifier,
        DecorationModifier(),
        LegacyChatFormattingModifier(),
        ClickEventModifier,
        HoverEventModifier,
        ColorModifier
    )

    val noneEventModifier = arrayOf(
        TextContentModifier,
        DecorationModifier(),
        LegacyChatFormattingModifier(),
        ColorModifier
    )

    fun parse(content: String, textModifiers: Array<TextModifier> = allModifier): MutableComponent =
        decode(tokenize(content), textModifiers)


    fun inline(text: Text): String {
        return if (text !is MutableComponent) text.string
        else buildString {
            text.flat()
                .asSequence()
                .filterIsInstance<MutableComponent>()
                .filter { it.string.isNotEmpty() }
                .forEach { t ->
                    val style = t.style
                    val content = t.contents
                    if (!style.isEmpty) {
                        buildString {
                            append("&{")
                            if (content is TranslatableContents) append("ts=>${content.key},")
                            if (content is KeybindContents) append("kb=>${content.name},")
                            if (style.color != null) append("#${Color.fromRGB(style.color!!.value).hexStr},")
                            if (style.shadowColor != null) append("s#${Color.fromRGB(style.shadowColor!!).hexStr},")
                            if (style.bold != null) append(style.bold.either("b,", "!b,"))
                            if (style.italic != null) append(style.italic.either("i,", "!i,"))
                            if (style.underlined != null) append(style.underlined.either("l,", "!l,"))
                            if (style.strikethrough != null) append(style.strikethrough.either("s,", "!s,"))
                            if (style.obfuscated != null) append(style.obfuscated.either("o,", "!o,"))
                            append("}")
                            dropLastChar(',')
                        }.let { append(it) }
                    } else {
                        buildString {
                            append("&{")
                            if (content is TranslatableContents) append("ts=>${content.key},")
                            if (content is KeybindContents) append("kb=>${content.name},")
                            append("}")
                            dropLastChar(',')
                        }.let { append(it) }
                    }
                    append(t.plainText)
                }
        }
    }

    private fun StringBuilder.dropLastChar(char: Char): CharSequence {
        val index = lastIndexOf(char)
        return if (index != -1) {
            this.deleteCharAt(index)
        } else {
            this
        }
    }

    private sealed interface Token {
        object ControlStart : Token
        object ControlEnd : Token
        class Expression(val raw: String) : Token
        class Literal(val raw: String) : Token
    }

    private fun tokenize(input: String): List<Token> = buildList {
        val chars = input.toCharArray()
        var idx = 0

        while (idx < chars.size) {
            //检查是否匹配到&{
            if (idx + 1 < chars.size && chars[idx] == '&' && chars[idx + 1] == '{') {
                this += Token.ControlStart
                idx += 2
                //进入表达式内部处理逻辑
                var start = idx
                while (idx < chars.size && chars[idx] != '}') {
                    //检查分隔符 ,
                    if (chars[idx] == ',') {
                        if (idx > start) {
                            this += Token.Expression(input.substring(start, idx))
                        }
                        start = idx + 1
                    }
                    idx += 1
                }
                //收集最有一个表达式片断
                if (idx > start && idx <= chars.size) {
                    this += Token.ControlEnd
                    idx += 1
                }
            } else {
                //处理普通文本
                val start = idx
                while (idx < chars.size && !(idx + 1 < chars.size && chars[idx] == '&' && chars[idx + 1] == '{')) {
                    idx += 1
                }
                if (idx > start) {
                    this += Token.Expression(input.substring(start, idx))
                }
            }
        }

    }


    private fun decode(tokens: List<Token>, modifiers: Array<TextModifier>): MutableComponent {
        val pendingExpressions = mutableListOf<String>()
        val components = buildList {
            tokens.forEach { token ->
                when (token) {
                    Token.ControlStart  -> pendingExpressions.clear()
                    is Token.Expression -> pendingExpressions.add(token.raw)
                    is Token.Literal    -> {
                        var current = Texts.literal(token.raw)
                        pendingExpressions.forEach { expression ->
                            modifiers.forEach { modifier ->
                                modifier.modify(expression, current)?.let { current = it }
                            }
                        }
                        this.add(current)
                    }

                    else                -> {}
                }
            }
        }

        return when (components.size) {
            0    -> Texts.empty()
            1    -> components.first()
            else -> {
                Texts.empty().apply {
                    components.forEach { component -> append(component) }
                }
            }
        }
    }

}

