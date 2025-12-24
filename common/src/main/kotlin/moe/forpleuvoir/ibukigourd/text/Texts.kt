@file:Suppress("UNUSED")

package moe.forpleuvoir.ibukigourd.text

import moe.forpleuvoir.ibukigourd.text.inlinestyletext.InlineStyleTextParser
import moe.forpleuvoir.ibukigourd.text.style.StyleBuilder
import moe.forpleuvoir.nebula.common.color.RGBColor
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
        return MutableText.create(PlainTextContents.create(""))
    }

    @JvmStatic
    @JvmOverloads
    fun translatable(key: String, fallback: String? = null, vararg args: Any): MutableText {
        return MutableText.create(TranslatableContents(key, fallback, args))
    }

    @JvmStatic
    fun keyBind(translationKey: String): MutableText {
        return MutableText.create(KeybindContents(translationKey))
    }

}

val MutableText.plainText: String get() = this.string

fun MutableText.appendLiteral(content: String) = this.append(content)

fun MutableText.appendNewLine() = this.appendLiteral("\n")

fun MutableText.appendTranslate(key: String, fallback: String? = null, vararg args: Any) = this.append(Texts.translatable(key, fallback, *args))

@OptIn(ExperimentalContracts::class)
inline fun MutableText.append(text: () -> Text): MutableText {
    contract {
        callsInPlace(text, InvocationKind.EXACTLY_ONCE)
    }
    return this.append(text())
}

fun MutableText.style(builder: StyleBuilder.() -> Unit) = withStyle { StyleBuilder(it).apply(builder).asStyle }

fun MutableText.withColor(rgbColor: RGBColor) = withColor(rgbColor.rgb)

fun MutableText.withShadowColor(rgbColor: RGBColor) = style { shadowColor(rgbColor) }
