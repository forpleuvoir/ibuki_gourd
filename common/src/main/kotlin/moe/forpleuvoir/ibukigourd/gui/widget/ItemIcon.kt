package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.attachLeft
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.render
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.size
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateBy
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike

@JvmName("ItemStackIcon")
fun ContainerScope.ItemIcon(
    item: State<ItemStack>,
    scale: Float = 1f,
    hoverScale: Float = 1f,
    modifier: Modifier = Modifier
) = Widget(modifier.attachLeft {
    size(16f * scale, 16f * scale)
        .render { guiGraphics, _, _, _ ->
            val pos = transform.worldPosition
            if (wasMouseOver && hoverScale != 1f) {
                val w = (transform.width * hoverScale - transform.width) * 0.5f
                val h = (transform.height * hoverScale - transform.height) * 0.5f
                guiGraphics.pushItem(item.getValue(), pos.x() - w, pos.y() - h, scale * hoverScale)
            } else {
                guiGraphics.pushItem(item.getValue(), pos.x(), pos.y(), scale)
            }
        }
})

fun ContainerScope.ItemIcon(
    item: ItemStack,
    scale: Float = 1f,
    hoverScale: Float = 1f,
    modifier: Modifier = Modifier
) = ItemIcon(stateOf(item), scale, hoverScale, modifier)


fun ContainerScope.ItemIcon(
    item: ItemLike,
    scale: Float = 1f,
    hoverScale: Float = 1f,
    modifier: Modifier = Modifier
) = ItemIcon(stateOf(ItemStack(item)), scale, hoverScale, modifier)

fun ContainerScope.ItemIcon(
    item: State<out ItemLike>,
    scale: Float = 1f,
    hoverScale: Float = 1f,
    modifier: Modifier = Modifier
) = ItemIcon(mutableStateBy { ItemStack(item.getValue()) }, scale, hoverScale, modifier)