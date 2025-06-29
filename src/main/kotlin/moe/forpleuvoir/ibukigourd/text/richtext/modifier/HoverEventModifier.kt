package moe.forpleuvoir.ibukigourd.text.richtext.modifier

import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.richtext.RichText
import moe.forpleuvoir.ibukigourd.text.style.style
import net.minecraft.text.MutableText

object HoverEventModifier : TextModifier {

    private val pattern = """hover=>.+""".toRegex()

    private val supportedModifiers = arrayOf(
        DecorationModifier(),
        ColorModifier,
        LegacyChatFormattingModifier()
    )

    override fun modifier(exp: String): ((MutableText) -> MutableText)? {
        if (!exp.matches(pattern)) return null
        val content = exp.split("=>")[1]
        return { text ->
            text.style {
                hover(Text {
                    val lines = content.split("\\n")
                    lines.forEach { line ->
                        RichText.parse(line, supportedModifiers).let {
                            append(it)
                            if (line != lines.last()) newLine()
                        }
                    }
                })
            }
        }
    }

}