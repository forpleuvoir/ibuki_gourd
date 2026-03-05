package moe.forpleuvoir.ibukigourd.text.style

import moe.forpleuvoir.nebula.common.color.ARGBColor
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.RGBColor
import net.minecraft.network.chat.*
import net.minecraft.resources.ResourceLocation

fun style(
    color: RGBColor? = null,
    shadowColor: RGBColor? = null,
    bold: Boolean? = null,
    italic: Boolean? = null,
    underlined: Boolean? = null,
    strikethrough: Boolean? = null,
    obfuscated: Boolean? = null,
    clickEvent: ClickEvent? = null,
    hoverEvent: HoverEvent? = null,
    insertion: String? = null,
    font: ResourceLocation? = null
) = Style(
    color?.let { TextColor.fromRgb(it.rgb) },
    shadowColor?.rgb,
    bold,
    italic,
    underlined,
    strikethrough,
    obfuscated,
    clickEvent,
    hoverEvent,
    insertion,
    font
)

val Style.rgbColor: RGBColor?
    get() = argbColor

val Style.argbColor: ARGBColor?
    get() = this.color?.let { Color.ofRGB(it.value) }

fun Style.withColor(color: Int?): Style {
    return this.withColor(color?.let { TextColor.fromRgb(it) })
}

fun Style.color(rgbColor: RGBColor?): Style {
    return this.withColor(rgbColor?.rgb)
}

fun Style.withShadowColor(shadowColor: Int?): Style {
    return Style(
        color,
        shadowColor,
        bold,
        italic,
        underlined,
        strikethrough,
        obfuscated,
        clickEvent,
        hoverEvent,
        insertion,
        font
    )
}

fun Style.shadowColor(shadowColor: RGBColor?): Style {
    return this.withShadowColor(shadowColor?.rgb)
}

fun Style.bold(bold: Boolean?): Style {
    return this.withBold(bold)
}

fun Style.italic(italic: Boolean?): Style {
    return this.withItalic(italic)
}

fun Style.underlined(underlined: Boolean?): Style {
    return this.withUnderlined(underlined)
}

fun Style.strikethrough(strikethrough: Boolean?): Style {
    return this.withStrikethrough(strikethrough)
}

fun Style.obfuscated(obfuscated: Boolean?): Style {
    return this.withObfuscated(obfuscated)
}

fun Style.clickEvent(clickEvent: ClickEvent?): Style {
    return this.withClickEvent(clickEvent)
}

fun Style.hoverEvent(hoverEvent: HoverEvent?): Style {
    return this.withHoverEvent(hoverEvent)
}

fun Style.insertion(insertion: String?): Style {
    return this.withInsertion(insertion)
}

fun Style.font(font: ResourceLocation?): Style {
    return this.withFont(font)
}

