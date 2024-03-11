package moe.forpleuvoir.ibukigourd.text.style

import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.RGBColor
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.TextColor
import net.minecraft.util.Identifier

fun Style.withColor(color: RGBColor): Style {
    return this.withColor(color.rgb)
}

val Style.rgbColor: RGBColor?
    get() {
        return this.color?.let { Color(it.rgb) }
    }

fun style(
    color: RGBColor? = null,
    bold: Boolean? = null,
    italic: Boolean? = null,
    underlined: Boolean? = null,
    strikethrough: Boolean? = null,
    obfuscated: Boolean? = null,
    clickEvent: ClickEvent? = null,
    hoverEvent: HoverEvent? = null,
    insertion: String? = null,
    font: Identifier? = null
): Style {
    return Style(color?.let { TextColor.fromRgb(it.rgb) }, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent, insertion, font)
}

fun Style.color(rgbColor: RGBColor): Style {
    return this.withColor(rgbColor)
}

fun Style.bold(bold: Boolean): Style {
    return this.withBold(bold)
}

fun Style.italic(italic: Boolean): Style {
    return this.withItalic(italic)
}

fun Style.underlined(underlined: Boolean): Style {
    return this.withUnderline(underlined)
}

fun Style.strikethrough(strikethrough: Boolean): Style {
    return this.withStrikethrough(strikethrough)
}

fun Style.obfuscated(obfuscated: Boolean): Style {
    return this.withObfuscated(obfuscated)
}

fun Style.clickEvent(clickEvent: ClickEvent): Style {
    return this.withClickEvent(clickEvent)
}

fun Style.hoverEvent(hoverEvent: HoverEvent): Style {
    return this.withHoverEvent(hoverEvent)
}

fun Style.insertion(insertion: String): Style {
    return this.withInsertion(insertion)
}

fun Style.font(font: Identifier): Style {
    return this.withFont(font)
}

