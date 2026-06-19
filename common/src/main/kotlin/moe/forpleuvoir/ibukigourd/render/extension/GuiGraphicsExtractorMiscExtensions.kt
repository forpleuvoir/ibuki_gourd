package moe.forpleuvoir.ibukigourd.render.extension

import androidx.compose.ui.layout.ScaleFactor
import moe.forpleuvoir.ibukigourd.util.textRenderer
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.item.ItemStack

//region Item
fun GuiGraphicsExtractor.pushItem(
    itemStack: ItemStack,
    x: Float,
    y: Float,
    scale: ScaleFactor = ScaleFactor(1f, 1f),
    showCount: Boolean = false,
    seed: Int = 0
) {
    val x = x / scale.scaleX
    val y = y / scale.scaleY
    val xi = x.toInt()
    val yi = y.toInt()

    pose().pushMatrix()
    pose().scale(scale.scaleX, scale.scaleY)
    pose().translate(x - xi, y - yi)

    fakeItem(itemStack, xi, yi, seed)

    if (showCount && itemStack.count > 1) {
        val count = itemStack.count.toString()
        text(textRenderer, count, xi + 16 - textRenderer.width(count), yi + textRenderer.lineHeight - 1, -1, true)
    }

    pose().popMatrix()
}
//endregion