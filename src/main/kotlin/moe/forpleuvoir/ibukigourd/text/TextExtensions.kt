@file:Suppress("UNUSED")

package moe.forpleuvoir.ibukigourd.text

import net.minecraft.text.MutableText
import net.minecraft.text.Text as McText

@JvmOverloads
fun Literal(content: String = ""): Text = Text.literal(content)

@JvmOverloads
fun Translatable(key: String, fallback: String? = null, vararg args: Any): Text = Text.translatable(key, fallback, *args)

fun MutableText.flat(): List<McText> {
    return buildList {
        add(Text.literal(this@flat.string).styled { this@flat.style })
        this@flat.siblings.forEach { text ->
            if (text is MutableText) {
                addAll(text.flat())
            } else {
                add(text)
            }
        }
    }
}

