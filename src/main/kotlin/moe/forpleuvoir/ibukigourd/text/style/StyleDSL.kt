package moe.forpleuvoir.ibukigourd.text.style

import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.RGBColor
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.text.*
import net.minecraft.util.Identifier

fun MutableText.style(style: StyleScope.() -> Unit): MutableText {
    return this.styled {
        StyleScope(it).apply(style).asStyle
    }
}

class StyleScope(parent: Style) {

    val asStyle: Style get() = style(color, shadowColor, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent)

    private var color: RGBColor? = parent.color?.let { Color(it.rgb).alpha(1f) }

    private var shadowColor: RGBColor? = parent.shadowColor?.let { Color(it).alpha(1f) }

    private var bold: Boolean? = parent.bold

    private var italic: Boolean? = parent.italic

    private var underlined: Boolean? = parent.underlined

    private var strikethrough: Boolean? = parent.strikethrough

    private var obfuscated: Boolean? = parent.obfuscated

    private var clickEvent: ClickEvent? = parent.clickEvent

    private var hoverEvent: HoverEvent? = parent.hoverEvent

    private var insertion: String? = parent.insertion

    private var font: Identifier? = parent.font

    fun color(rgbColor: RGBColor?): StyleScope {
        this.color = rgbColor
        return this
    }

    fun color(rgbColor: Int?): StyleScope {
        this.color = rgbColor?.let { Color(rgbColor).alpha(1f) }
        return this
    }

    fun color(hexColor: String?): StyleScope {
        this.color = hexColor?.let { Color(it) }
        return this
    }

    fun shadowColor(rgbColor: RGBColor?): StyleScope {
        this.shadowColor = rgbColor
        return this
    }

    fun shadowColor(rgbColor: Int?): StyleScope {
        this.shadowColor = rgbColor?.let { Color(it).alpha(1f) }
        return this
    }

    fun bold(bold: Boolean? = true): StyleScope {
        this.bold = bold
        return this
    }

    fun italic(italic: Boolean? = true): StyleScope {
        this.italic = italic
        return this
    }

    fun underlined(underlined: Boolean? = true): StyleScope {
        this.underlined = underlined
        return this
    }

    fun strikethrough(strikethrough: Boolean? = true): StyleScope {
        this.strikethrough = strikethrough
        return this
    }

    fun obfuscated(obfuscated: Boolean? = true): StyleScope {
        this.obfuscated = obfuscated
        return this
    }

    fun clickEvent(clickEvent: ClickEvent?): StyleScope {
        this.clickEvent = clickEvent
        return this
    }

    @Suppress("DuplicatedCode")
    inline fun <reified T : ClickEventAction> click(value: String): StyleScope {
        return clickEvent(
            when (T::class) {
                OpenUrl::class         -> ClickEvent(ClickEvent.Action.OPEN_URL, value)
                OpenFile::class        -> ClickEvent(ClickEvent.Action.OPEN_FILE, value)
                RunCommand::class      -> ClickEvent(ClickEvent.Action.RUN_COMMAND, value)
                SuggestCommand::class  -> ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value)
                ChangePage::class      -> ClickEvent(ClickEvent.Action.CHANGE_PAGE, value)
                CopyToClipboard::class -> ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value)
                else                   -> throw IllegalArgumentException("Unknown click event action: ${T::class.java.simpleName}")
            }
        )
    }

    fun hoverEvent(hoverEvent: HoverEvent?): StyleScope {
        this.hoverEvent = hoverEvent
        return this
    }

    fun hover(itemStack: ItemStack): StyleScope {
        hoverEvent(HoverEvent(HoverEvent.Action.SHOW_ITEM, HoverEvent.ItemStackContent(itemStack)))
        return this
    }

    fun hover(itemStackContent: HoverEvent.ItemStackContent): StyleScope {
        hoverEvent(HoverEvent(HoverEvent.Action.SHOW_ITEM, itemStackContent))
        return this
    }

    fun hover(entity: Entity): StyleScope {
        hoverEvent(HoverEvent(HoverEvent.Action.SHOW_ENTITY, HoverEvent.EntityContent(entity.type, entity.uuid, entity.displayName)))
        return this
    }

    fun hover(entityContent: HoverEvent.EntityContent): StyleScope {
        hoverEvent(HoverEvent(HoverEvent.Action.SHOW_ENTITY, entityContent))
        return this
    }

    fun hover(text: Text): StyleScope {
        hoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, text))
        return this
    }

    fun insertion(insertion: String?): StyleScope {
        this.insertion = insertion
        return this
    }

    fun font(font: Identifier?): StyleScope {
        this.font = font
        return this
    }

}
