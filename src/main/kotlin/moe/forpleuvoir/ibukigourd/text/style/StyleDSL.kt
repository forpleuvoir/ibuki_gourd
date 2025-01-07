package moe.forpleuvoir.ibukigourd.text.style

import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.RGBColor
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.text.*
import net.minecraft.util.Identifier

fun MutableText.style(style: StyleScope.() -> Unit): MutableText {
    return this.styled {
        StyleScope().apply(style).style
    }
}

class StyleScope {

    val style: Style get() = style(color, shadowColor, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent)

    private var color: RGBColor? = null

    private var shadowColor: RGBColor? = null

    private var bold: Boolean? = null

    private var italic: Boolean? = null

    private var underlined: Boolean? = null

    private var strikethrough: Boolean? = null

    private var obfuscated: Boolean? = null

    private var clickEvent: ClickEvent? = null

    private var hoverEvent: HoverEvent? = null

    private var insertion: String? = null

    private var font: Identifier? = null

    fun color(rgbColor: RGBColor): StyleScope {
        this.color = rgbColor
        return this
    }

    fun color(rgbColor: Int): StyleScope {
        this.color = Color(rgbColor).alpha(1f)
        return this
    }

    fun color(hexColor: String): StyleScope {
        this.color = Color(hexColor)
        return this
    }

    fun shadowColor(rgbColor: RGBColor): StyleScope {
        this.shadowColor = rgbColor
        return this
    }

    fun shadowColor(rgbColor: Int): StyleScope {
        this.shadowColor = Color(rgbColor).alpha(1f)
        return this
    }

    fun bold(bold: Boolean = true): StyleScope {
        this.bold = bold
        return this
    }

    fun italic(italic: Boolean = true): StyleScope {
        this.italic = italic
        return this
    }

    fun underlined(underlined: Boolean = true): StyleScope {
        this.underlined = underlined
        return this
    }

    fun strikethrough(strikethrough: Boolean = true): StyleScope {
        this.strikethrough = strikethrough
        return this
    }

    fun obfuscated(obfuscated: Boolean = true): StyleScope {
        this.obfuscated = obfuscated
        return this
    }

    fun clickEvent(clickEvent: ClickEvent): StyleScope {
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

    fun hoverEvent(hoverEvent: HoverEvent): StyleScope {
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

    fun insertion(insertion: String): StyleScope {
        this.insertion = insertion
        return this
    }

    fun font(font: Identifier): StyleScope {
        this.font = font
        return this
    }

}
