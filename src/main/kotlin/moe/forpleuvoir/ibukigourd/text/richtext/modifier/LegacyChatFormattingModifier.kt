package moe.forpleuvoir.ibukigourd.text.richtext.modifier

import net.minecraft.text.MutableText
import net.minecraft.util.Formatting

@Suppress("SpellCheckingInspection")
const val LEGACY_FORMAT_CHARS = "0123456789abcdefklmonr"

class LegacyChatFormattingModifier(
    private val prefixChar: Char = '$',
    formatChars: Set<Char> = LEGACY_FORMAT_CHARS.toSet(),
) : TextModifier {

    private val formatChars: Set<Char> = fixFormatChars(formatChars)

    private fun fixFormatChars(formatChars: Set<Char>): Set<Char> {
        return buildSet {
            formatChars.forEach { c ->
                if (c in LEGACY_FORMAT_CHARS) {
                    add(c)
                }
            }
        }

    }

    override fun modifier(exp: String): ((MutableText) -> MutableText)? {
        if (exp.startsWith(prefixChar) && exp.length == 2) {
            return { text ->
                if (exp[1] in formatChars) {
                    text.formatted(Formatting.byCode(exp[1]))
                } else text
            }
        }
        return null
    }

}