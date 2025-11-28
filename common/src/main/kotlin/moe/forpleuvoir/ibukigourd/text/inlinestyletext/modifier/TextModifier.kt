package moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier

import net.minecraft.network.chat.MutableComponent

fun interface TextModifier {
    fun modifier(exp: String): ((MutableComponent) -> MutableComponent)?
}