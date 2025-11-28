package moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier

import moe.forpleuvoir.ibukigourd.text.buildText
import moe.forpleuvoir.ibukigourd.text.inlinestyletext.InlineStyleTextParser
import moe.forpleuvoir.ibukigourd.text.style.style
import net.minecraft.network.chat.MutableComponent

object HoverEventModifier : TextModifier {

    private val pattern = """hover=>.+""".toRegex()

    override fun modifier(exp: String): ((MutableComponent) -> MutableComponent)? {
        if (!exp.matches(pattern)) return null
        val content = exp.split("=>")[1]
        return { text ->
            text.style {
                hover(buildText {
                    val lines = content.split("\\n")
                    lines.forEach { line ->
                        InlineStyleTextParser.parse(line, InlineStyleTextParser.noneEventModifier).let {
                            append(it)
                            if (line != lines.last()) newLine()
                        }
                    }
                })
            }
        }
    }

}