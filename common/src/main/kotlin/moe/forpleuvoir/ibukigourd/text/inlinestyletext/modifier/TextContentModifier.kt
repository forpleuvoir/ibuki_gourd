package moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier

import moe.forpleuvoir.ibukigourd.text.McText
import net.minecraft.network.chat.MutableComponent

object TextContentModifier : TextModifier {
    override fun modifier(exp: String): ((MutableComponent) -> MutableComponent)? {
        if (exp.startsWith("c:")) {
            val exp = exp.substring(2)
            translationContent(exp)?.let { return it }
            translationContent(exp)?.let { return it }
        }
        return null
    }

    private fun translationContent(exp: String): ((MutableComponent) -> MutableComponent)? {
        return if (exp == "ts") {
            { text ->
                val key = text.string
                McText.translatable(key).setStyle(text.style)
            }
        } else null
    }

    private fun keybindContent(exp: String): ((MutableComponent) -> MutableComponent)? {
        return if (exp == "kb") {
            { text ->
                val key = text.string
                McText.keybind(key).setStyle(text.style)
            }
        } else null
    }

}