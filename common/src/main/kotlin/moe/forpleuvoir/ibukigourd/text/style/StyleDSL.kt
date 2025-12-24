package moe.forpleuvoir.ibukigourd.text.style

import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.RGBColor
import net.minecraft.network.chat.*
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack

fun MutableComponent.style(style: StyleBuilder.() -> Unit): MutableComponent {
    return this.withStyle {
        StyleBuilder(it).apply(style).asStyle
    }
}

class StyleBuilder(parent: Style) {

    val asStyle: Style get() = style(color, shadowColor, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent)

    private var color: RGBColor? = parent.color?.let { Color.ofRGB(it.value) }

    private var shadowColor: RGBColor? = parent.shadowColor?.let { Color.ofRGB(it) }

    private var bold: Boolean? = parent.bold

    private var italic: Boolean? = parent.italic

    private var underlined: Boolean? = parent.underlined

    private var strikethrough: Boolean? = parent.strikethrough

    private var obfuscated: Boolean? = parent.obfuscated

    private var clickEvent: ClickEvent? = parent.clickEvent

    private var hoverEvent: HoverEvent? = parent.hoverEvent

    private var insertion: String? = parent.insertion

    private var font: FontDescription? = parent.font

    fun color(rgbColor: RGBColor?): StyleBuilder {
        this.color = rgbColor
        return this
    }

    fun color(rgbColor: Int?): StyleBuilder {
        this.color = rgbColor?.let { Color.ofRGB(it) }
        return this
    }

    fun color(hexColor: String?): StyleBuilder {
        this.color = hexColor?.let { Color.ofString(it) }
        return this
    }

    fun shadowColor(rgbColor: RGBColor?): StyleBuilder {
        this.shadowColor = rgbColor
        return this
    }

    fun shadowColor(rgbColor: Int?): StyleBuilder {
        this.shadowColor = rgbColor?.let { Color.ofRGB(it) }
        return this
    }

    fun bold(bold: Boolean? = true): StyleBuilder {
        this.bold = bold
        return this
    }

    fun italic(italic: Boolean? = true): StyleBuilder {
        this.italic = italic
        return this
    }

    fun underlined(underlined: Boolean? = true): StyleBuilder {
        this.underlined = underlined
        return this
    }

    fun strikethrough(strikethrough: Boolean? = true): StyleBuilder {
        this.strikethrough = strikethrough
        return this
    }

    fun obfuscated(obfuscated: Boolean? = true): StyleBuilder {
        this.obfuscated = obfuscated
        return this
    }

    fun clickEvent(clickEvent: ClickEvent?): StyleBuilder {
        this.clickEvent = clickEvent
        return this
    }

    fun hoverEvent(hoverEvent: HoverEvent?): StyleBuilder {
        this.hoverEvent = hoverEvent
        return this
    }

    fun hover(itemStack: ItemStack): StyleBuilder {
        hoverEvent(HoverEvent.ShowItem(itemStack))
        return this
    }

    fun hover(entity: Entity): StyleBuilder {
        hoverEvent(HoverEvent.ShowEntity(HoverEvent.EntityTooltipInfo(entity.type, entity.uuid, entity.displayName)))
        return this
    }

    fun hover(entityTooltipInfo: HoverEvent.EntityTooltipInfo): StyleBuilder {
        hoverEvent(HoverEvent.ShowEntity(entityTooltipInfo))
        return this
    }

    fun hover(text: Component): StyleBuilder {
        hoverEvent(HoverEvent.ShowText(text))
        return this
    }

    fun insertion(insertion: String?): StyleBuilder {
        this.insertion = insertion
        return this
    }

    fun font(font: FontDescription?): StyleBuilder {
        this.font = font
        return this
    }

}
