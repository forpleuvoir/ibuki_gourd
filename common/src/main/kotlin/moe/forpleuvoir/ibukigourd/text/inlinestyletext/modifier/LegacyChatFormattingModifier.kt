package moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.MutableComponent

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

    override fun modifier(exp: String): ((MutableComponent) -> MutableComponent)? {
        if (exp.startsWith(prefixChar) && exp.length == 2) {
            return { text ->
                if (exp[1] in formatChars) {
                    text.withStyle(ChatFormatting.getByCode(exp[1])!!)
                } else text
            }
        }
        return null
    }

}