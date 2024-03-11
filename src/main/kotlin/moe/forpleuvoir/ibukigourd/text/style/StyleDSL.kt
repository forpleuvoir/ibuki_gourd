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

    val style: Style get() = style(color, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent)

    private var color: RGBColor? = null

    private var bold: Boolean? = null

    private var italic: Boolean? = null

    private var underlined: Boolean? = null

    private var strikethrough: Boolean? = null

    private var obfuscated: Boolean? = null

    private var clickEvent: ClickEvent? = null

    private var hoverEvent: HoverEvent? = null

    private var insertion: String? = null

    private var font: Identifier? = null

    fun color(rgbColor: RGBColor) {
        this.color = rgbColor
    }

    fun color(rgbColor: Int) {
        this.color = Color(rgbColor).alpha(1f)
    }

    fun bold(bold: Boolean = true) {
        this.bold = bold
    }

    fun italic(italic: Boolean = true) {
        this.italic = italic
    }

    fun underlined(underlined: Boolean = true) {
        this.underlined = underlined
    }

    fun strikethrough(strikethrough: Boolean = true) {
        this.strikethrough = strikethrough
    }

    fun obfuscated(obfuscated: Boolean = true) {
        this.obfuscated = obfuscated
    }

    fun clickEvent(clickEvent: ClickEvent) {
        this.clickEvent = clickEvent
    }

    inline fun <reified T : ClickEventAction> click(value: String) {
        clickEvent(
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

    fun hoverEvent(hoverEvent: HoverEvent) {
        this.hoverEvent = hoverEvent
    }

    fun hover(itemStack: ItemStack) {
        hoverEvent(HoverEvent(HoverEvent.Action.SHOW_ITEM, HoverEvent.ItemStackContent(itemStack)))
    }

    fun hover(itemStackContent: HoverEvent.ItemStackContent) {
        hoverEvent(HoverEvent(HoverEvent.Action.SHOW_ITEM, itemStackContent))
    }

    fun hover(entity: Entity) {
        hoverEvent(HoverEvent(HoverEvent.Action.SHOW_ENTITY, HoverEvent.EntityContent(entity.type, entity.uuid, entity.displayName)))
    }

    fun hover(entityContent: HoverEvent.EntityContent) {
        hoverEvent(HoverEvent(HoverEvent.Action.SHOW_ENTITY, entityContent))
    }

    fun hover(text: Text) {
        hoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, text))
    }

    fun insertion(insertion: String) {
        this.insertion = insertion
    }

    fun font(font: Identifier) {
        this.font = font
    }

}
