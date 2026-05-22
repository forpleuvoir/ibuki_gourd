package moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier

import net.minecraft.network.chat.MutableComponent

fun interface TextModifier {
    fun modify(exp: String, current: MutableComponent): MutableComponent?
}