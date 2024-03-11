package moe.forpleuvoir.ibukigourd.text.style

import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.text.HoverEvent
import net.minecraft.text.HoverEvent.EntityContent
import net.minecraft.text.HoverEvent.ItemStackContent
import net.minecraft.text.Style
import net.minecraft.text.Text

fun Style.hover(itemStack: ItemStack): Style {
    this.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_ITEM, ItemStackContent(itemStack)))
    return this
}

fun Style.hover(itemStackContent: ItemStackContent): Style {
    this.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_ITEM, itemStackContent))
    return this
}

fun Style.hover(entity: Entity): Style {
    this.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_ENTITY, EntityContent(entity.type, entity.uuid, entity.displayName)))
    return this
}

fun Style.hover(entityContent: EntityContent): Style {
    this.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_ENTITY, entityContent))
    return this
}

fun Style.hover(text: Text): Style {
    this.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, text))
    return this
}
