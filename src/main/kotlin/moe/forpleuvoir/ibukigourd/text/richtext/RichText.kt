package moe.forpleuvoir.ibukigourd.text.richtext

import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.richtext.modifier.*
import net.minecraft.text.MutableText

object RichText {

    @JvmStatic
    val regex = Regex("&\\{.*?}")

    fun parse(
        content: String, textModifiers: Array<TextModifier> = arrayOf(
            DecorationModifier(),
            ColorModifier,
            LegacyChatFormattingModifier(),
            ClickEventModifier,
            HoverEventModifier,
        )
    ): MutableText {
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
            }.split(regex).toMutableList().let { list ->
                literal(list[0])
                modifiers.forEachIndexed { index, modifier ->
                    var t: MutableText = Literal(list[index + 1])
                    modifier.forEach { m ->
                        t = m.invoke(t)
                    }
                    append(t)
                }
            }
        }
    }


}
