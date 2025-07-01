package moe.forpleuvoir.ibukigourd.text.inline_style_text.modifier

import net.minecraft.text.MutableText

fun interface TextModifier {
    fun modifier(exp: String): ((MutableText) -> MutableText)?
}