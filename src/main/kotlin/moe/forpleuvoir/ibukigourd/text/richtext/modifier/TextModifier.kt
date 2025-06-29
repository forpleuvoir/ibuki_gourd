package moe.forpleuvoir.ibukigourd.text.richtext.modifier

import net.minecraft.text.MutableText

fun interface TextModifier {
    fun modifier(exp: String): ((MutableText) -> MutableText)?
}