package moe.forpleuvoir.ibukigourd.text.style

import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.network.chat.*
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate

fun MutableComponent.style(style: StyleBuilder.() -> Unit): MutableComponent {
    return this.withStyle {
        StyleBuilder(it).apply(style).asStyle
    }
}

class StyleBuilder(parent: Style) {

    val asStyle: Style get() = style(color, shadowColor, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent)

    private var color: Color? = parent.color?.let { Color.fromRGB(it.value) }

    private var shadowColor: Color? = parent.shadowColor?.let { Color.fromRGB(it) }

    private var bold: Boolean? = parent.bold

    private var italic: Boolean? = parent.italic

    private var underlined: Boolean? = parent.underlined

    private var strikethrough: Boolean? = parent.strikethrough

    private var obfuscated: Boolean? = parent.obfuscated

    private var clickEvent: ClickEvent? = parent.clickEvent

    private var hoverEvent: HoverEvent? = parent.hoverEvent

    private var insertion: String? = parent.insertion

    private var font: FontDescription? = parent.font

    fun color(rgbColor: Color?): StyleBuilder {
        this.color = rgbColor
        return this
    }

    fun color(rgbColor: Int?): StyleBuilder {
        this.color = rgbColor?.let { Color.fromRGB(it) }
        return this
    }

    fun color(hexColor: String?): StyleBuilder {
        this.color = hexColor?.let { Color.fromHexString(it) }
        return this
    }

    fun shadowColor(rgbColor: Color?): StyleBuilder {
        this.shadowColor = rgbColor
        return this
    }

    fun shadowColor(rgbColor: Int?): StyleBuilder {
        this.shadowColor = rgbColor?.let { Color.fromRGB(it) }
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
        hoverEvent(HoverEvent.ShowItem(ItemStackTemplate.fromNonEmptyStack(itemStack)))
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
