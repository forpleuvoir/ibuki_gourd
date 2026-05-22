package moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier

import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.nebula.common.util.primitive.either
import net.minecraft.network.chat.MutableComponent

class DecorationMapping(
    val obfuscated: Array<String> = arrayOf("o", "obfuscated"),
    val bold: Array<String> = arrayOf("b", "bold"),
    val italic: Array<String> = arrayOf("i", "italic"),
    val strikethrough: Array<String> = arrayOf("s", "strikethrough"),
    val underline: Array<String> = arrayOf("l", "underline"),
    val rest: Array<String> = arrayOf("r", "rest"),
) {
    companion object {
        val DEFAULT = DecorationMapping()
    }
}

class DecorationModifier(val decorationMapping: DecorationMapping = DecorationMapping.DEFAULT) : TextModifier {
    override fun modify(exp: String, current: MutableComponent): MutableComponent? {
        if (exp.isBlank()) return null

        val (cleanExp, state) =
            if (exp.startsWith("!")) exp.substring(1) to false
            else if (exp.startsWith("?")) exp.substring(1) to null
            else exp to false

        return when (cleanExp) {
            in decorationMapping.obfuscated    -> current.style { obfuscated(state) }
            in decorationMapping.bold          -> current.style { bold(state) }
            in decorationMapping.strikethrough -> current.style { strikethrough(state) }
            in decorationMapping.underline     -> current.style { underlined(state) }
            in decorationMapping.italic        -> current.style { italic(state) }
            in decorationMapping.rest          -> current.withStyle { style() }
            else                               -> null
        }
    }

}