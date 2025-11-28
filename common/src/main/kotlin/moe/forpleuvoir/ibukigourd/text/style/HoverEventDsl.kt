package moe.forpleuvoir.ibukigourd.text.style

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack

fun Style.hover(itemStack: ItemStack): Style {
    this.withHoverEvent(HoverEvent.ShowItem(itemStack))
    return this
}


fun Style.hover(entity: Entity): Style {
    this.withHoverEvent(HoverEvent.ShowEntity(HoverEvent.EntityTooltipInfo(entity.type, entity.uuid, entity.displayName)))
    return this
}

fun Style.hover(entityTooltipInfo: HoverEvent.EntityTooltipInfo): Style {
    this.withHoverEvent(HoverEvent.ShowEntity(entityTooltipInfo))
    return this
}

fun Style.hover(text: Component): Style {
    this.withHoverEvent(HoverEvent.ShowText(text))
    return this
}
