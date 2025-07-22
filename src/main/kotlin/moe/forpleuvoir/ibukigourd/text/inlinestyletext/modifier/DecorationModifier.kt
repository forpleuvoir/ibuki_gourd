package moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier

import moe.forpleuvoir.ibukigourd.text.style.style
import moe.forpleuvoir.nebula.common.util.primitive.pick
import net.minecraft.text.MutableText

data class DecorationMapping(
    val obfuscated: Array<String> = arrayOf("o", "obfuscated"),
    val bold: Array<String> = arrayOf("b", "bold"),
    val italic: Array<String> = arrayOf("i", "italic"),
    val strikethrough: Array<String> = arrayOf("s", "strikethrough"),
    val underline: Array<String> = arrayOf("l", "underline"),
    val rest: Array<String> = arrayOf("r", "rest"),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DecorationMapping

        if (!obfuscated.contentEquals(other.obfuscated)) return false
        if (!bold.contentEquals(other.bold)) return false
        if (!italic.contentEquals(other.italic)) return false
        if (!strikethrough.contentEquals(other.strikethrough)) return false
        if (!underline.contentEquals(other.underline)) return false
        if (!rest.contentEquals(other.rest)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = obfuscated.contentHashCode()
        result = 31 * result + bold.contentHashCode()
        result = 31 * result + italic.contentHashCode()
        result = 31 * result + strikethrough.contentHashCode()
        result = 31 * result + underline.contentHashCode()
        result = 31 * result + rest.contentHashCode()
        return result
    }
}

data class DecorationModifier(val decorationMapping: DecorationMapping = DecorationMapping()) : TextModifier {
    override fun modifier(exp: String): ((MutableText) -> MutableText)? {
        val inverted = exp.startsWith('!')
        val cs = if (exp.isNotEmpty()) inverted.pick(exp.substring(1), exp) else exp
        return when (cs) {
            in decorationMapping.obfuscated    -> { text -> text.style { obfuscated(!inverted) } }
            in decorationMapping.bold          -> { text -> text.style { bold(!inverted) } }
            in decorationMapping.strikethrough -> { text -> text.style { strikethrough(!inverted) } }
            in decorationMapping.underline     -> { text -> text.style { underlined(!inverted) } }
            in decorationMapping.italic        -> { text -> text.style { italic(!inverted) } }
            in decorationMapping.rest          -> { text -> text.styled { style() } }
            else                               -> null
        }
    }

}