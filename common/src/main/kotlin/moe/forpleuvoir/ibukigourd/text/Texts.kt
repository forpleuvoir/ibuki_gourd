@file:Suppress("UNUSED")

package moe.forpleuvoir.ibukigourd.text

import androidx.compose.ui.graphics.toArgb
import moe.forpleuvoir.ibukigourd.text.inlinestyletext.InlineStyleTextParser
import moe.forpleuvoir.ibukigourd.text.style.StyleBuilder
import moe.forpleuvoir.ibukigourd.ui.util.toNebulaColor
import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.KeybindContents
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.network.chat.contents.TranslatableContents
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

typealias MutableText = MutableComponent

object Texts {

    @JvmStatic
    fun inlineStyle(exp: String): MutableText {
        return InlineStyleTextParser.parse(exp)
    }

    @JvmStatic
    fun literal(content: String): MutableText {
        return MutableText.create(PlainTextContents.create(content))
    }

    @JvmStatic
    fun empty(): MutableText {
        return Component.empty()
    }

    @JvmStatic
    @JvmOverloads
    fun translatable(key: String, fallback: String? = null, vararg args: Any): MutableText {
        return MutableText.create(TranslatableContents(key, fallback, args))
    }

    @JvmStatic
    fun keybind(translationKey: String): MutableText {
        return MutableText.create(KeybindContents(translationKey))
    }

}

inline val Text.plainText: String get() = this.string

fun MutableText.appendLiteral(content: String) = this.append(content)

fun MutableText.appendLTRArrow() = this.appendLiteral(" → ")
fun MutableText.appendRTLArrow() = this.appendLiteral(" ← ")

fun MutableText.appendNewLine() = this.appendLiteral("\n")

fun MutableText.appendTranslate(key: String, fallback: String? = null, vararg args: Any) = this.append(Translatable(key, fallback, *args))

@OptIn(ExperimentalContracts::class)
inline fun MutableText.append(text: () -> Text): MutableText {
    contract {
        callsInPlace(text, InvocationKind.EXACTLY_ONCE)
    }
    return this.append(text())
}

fun MutableText.style(builder: StyleBuilder.() -> Unit) = withStyle { StyleBuilder(it).apply(builder).asStyle }

fun MutableText.withColor(rgbColor: Color) = withColor(rgbColor.rgb)

fun MutableText.withColor(rgbColor: androidx.compose.ui.graphics.Color) = withColor(rgbColor.toArgb())

fun MutableText.withShadowColor(rgbColor: Color) = style { shadowColor(rgbColor) }

fun MutableText.withShadowColor(rgbColor: androidx.compose.ui.graphics.Color) = style { shadowColor(rgbColor.toNebulaColor) }
