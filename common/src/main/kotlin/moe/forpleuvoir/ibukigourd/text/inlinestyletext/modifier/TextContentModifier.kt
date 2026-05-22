package moe.forpleuvoir.ibukigourd.text.inlinestyletext.modifier

import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.Texts
import net.minecraft.network.chat.MutableComponent

object TextContentModifier : TextModifier {

    private val pattern = """(ts|kb)=>(.+)""".toRegex()

    override fun modify(exp: String, current: MutableComponent): MutableComponent? {
        val (contentType, content) = pattern.matchEntire(exp)?.destructured ?: return null
        return when (contentType) {
            "ts" -> Text.translatable(content).setStyle(current.style)
            "kb" -> Text.keybind(content).setStyle(current.style)
            else -> null
        }
    }

}