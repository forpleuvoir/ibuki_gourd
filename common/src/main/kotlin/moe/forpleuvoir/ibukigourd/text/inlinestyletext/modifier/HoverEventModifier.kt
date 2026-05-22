package moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier

import moe.forpleuvoir.ibukigourd.text.inlinestyletext.InlineStyleTextParser
import moe.forpleuvoir.ibukigourd.text.style.style
import net.minecraft.network.chat.MutableComponent

object HoverEventModifier : TextModifier {

    private const val HOVER_PREFIX = "h=>"
    private const val HOVER_PREFIX_LENGTH = HOVER_PREFIX.length
    private const val NULL_EXP = "h=>null"

    override fun modify(exp: String, current: MutableComponent): MutableComponent? {
        if (exp == NULL_EXP) {
            return current.style { hoverEvent(null) }
        }
        return if (exp.startsWith(HOVER_PREFIX)) {
            val content = exp.substring(HOVER_PREFIX_LENGTH)
            if (content.isNotEmpty()) {
                val hoverContent = InlineStyleTextParser.parse(content, InlineStyleTextParser.noneEventModifier)
                current.style { hover(hoverContent) }
            }
            null
        } else null
    }

}