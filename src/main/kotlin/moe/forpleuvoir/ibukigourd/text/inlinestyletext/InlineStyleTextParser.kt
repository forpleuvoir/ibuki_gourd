package moe.forpleuvoir.ibukigourd.text.inlinestyletext

import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.McText
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.flat
import moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier.*
import moe.forpleuvoir.nebula.common.util.primitive.fillBefore
import moe.forpleuvoir.nebula.common.util.primitive.pick
import net.minecraft.text.KeybindTextContent
import net.minecraft.text.MutableText
import net.minecraft.text.TranslatableTextContent

object InlineStyleTextParser {

    @JvmStatic
    val regex = Regex("&\\{.*?}")

    val allModifier = arrayOf(
        TextContentModifier,
        DecorationModifier(),
        ColorModifier,
        LegacyChatFormattingModifier(),
        ClickEventModifier,
        HoverEventModifier,
    )

    val noneEventModifier = arrayOf(
        TextContentModifier,
        DecorationModifier(),
        ColorModifier,
        LegacyChatFormattingModifier(),
    )

    fun parse(content: String, textModifiers: Array<TextModifier> = allModifier): MutableText {
        val modifiers = ArrayList<List<(MutableText) -> MutableText>>()
        return Text {
            content.replace(regex) { result ->
                val value = result.value
                val list = mutableListOf<(MutableText) -> MutableText>()
                value.substring(2, value.length - 1).split(',').forEach { exp ->
                    textModifiers.forEach { modifier ->
                        modifier.modifier(exp)?.let(list::add)
                    }
                }
                modifiers.add(list)
                value
            }.split(regex).let { list ->
                if (list[0].isNotEmpty()) {
                    literal(list[0])
                }
                modifiers.forEachIndexed { index, modifier ->
                    var t: MutableText = Literal(list[index + 1])
                    modifier.forEach { m ->
                        t = m.invoke(t)
                    }
                    append(t)
                }
            }
            if (!isInitialized) {
                literal("")
            }
        }
    }

    fun inline(text: McText): String {
        if (text !is MutableText) return text.string
        return buildString {
            text.flat().forEach { t ->
                run {
                    if (t.string.isEmpty()) return@run
                    if (t is MutableText) {
                        val style = t.style
                        val content = t.content
                        if (!style.isEmpty) {
                            StringBuilder().apply {
                                append("&{")
                                if (content is TranslatableTextContent) {
                                    append("c:ts,")
                                }
                                if (content is KeybindTextContent) {
                                    append("c:kb,")
                                }
                                if (style.color != null) {
                                    append("#${style.color!!.rgb.toUInt().toString(16).fillBefore(6, '0').uppercase()},")
                                }
                                if (style.shadowColor != null) {
                                    append("s#${style.color!!.rgb.toUInt().toString(16).fillBefore(6, '0').uppercase()},")
                                }
                                if (style.bold != null) {
                                    append(style.bold.pick("b,", "!b,"))
                                }
                                if (style.italic != null) {
                                    append(style.italic.pick("i,", "!i,"))
                                }
                                if (style.underlined != null) {
                                    append(style.underlined.pick("l,", "!l,"))
                                }
                                if (style.strikethrough != null) {
                                    append(style.strikethrough.pick("s,", "!s,"))
                                }
                                if (style.obfuscated != null) {
                                    append(style.obfuscated.pick("o,", "!o,"))
                                }
                                append("}")
                            }.let {
                                append(it.dropLastChar(','))
                            }
                        } else {
                            StringBuilder().apply {
                                append("&{")
                                if (content is TranslatableTextContent) {
                                    append("c:ts,")
                                }
                                if (content is KeybindTextContent) {
                                    append("c:kb,")
                                }
                                dropLastWhile { it == ',' }
                                append("}")
                            }.let {
                                append(it.dropLastChar(','))
                            }
                        }
                        when (content) {
                            is TranslatableTextContent -> append(content.key)
                            is KeybindTextContent      -> append(content.key)
                            else                       -> append(t.string)
                        }
                    } else {
                        append("&{r}${t.string}")
                    }
                }
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


}
