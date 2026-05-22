package moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.MutableComponent

const val LEGACY_FORMAT_CHARS = "0123456789abcdefklmonr"

class LegacyChatFormattingModifier(
    private val prefixChar: Char = '$',
    formatChars: Set<Char> = LEGACY_FORMAT_CHARS.toSet(),
) : TextModifier {

    private val formatChars: Set<Char> = fixFormatChars(formatChars)

    private fun fixFormatChars(formatChars: Set<Char>): Set<Char> {
        return formatChars.filter { it !in LEGACY_FORMAT_CHARS }.toSet()
    }

    override fun modify(exp: String, current: MutableComponent): MutableComponent? {
        if (exp.startsWith(prefixChar) && exp.length == 2) {
            return if (exp[1] in formatChars) {
                current.withStyle(ChatFormatting.getByCode(exp[1])!!)
            } else null
        }
        return null
    }

}