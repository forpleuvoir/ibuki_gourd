package moe.forpleuvoir.ibukigourd.text.style

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate

fun Style.hover(itemStack: ItemStack): Style {
    return this.withHoverEvent(HoverEvent.ShowItem(ItemStackTemplate.fromNonEmptyStack(itemStack)))
}


fun Style.hover(entity: Entity): Style {
    return this.withHoverEvent(HoverEvent.ShowEntity(HoverEvent.EntityTooltipInfo(entity.type, entity.uuid, entity.displayName)))
}

fun Style.hover(entityTooltipInfo: HoverEvent.EntityTooltipInfo): Style {
    return this.withHoverEvent(HoverEvent.ShowEntity(entityTooltipInfo))
}

fun Style.hover(text: Component): Style {
    return this.withHoverEvent(HoverEvent.ShowText(text))
}
