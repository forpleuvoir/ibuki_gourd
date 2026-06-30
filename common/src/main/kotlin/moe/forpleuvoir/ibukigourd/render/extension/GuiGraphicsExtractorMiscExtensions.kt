package moe.forpleuvoir.ibukigourd.render.extension

import androidx.compose.ui.layout.ScaleFactor
import moe.forpleuvoir.ibukigourd.render.extension.state.setColor
import moe.forpleuvoir.ibukigourd.render.peekScissorRect
import moe.forpleuvoir.ibukigourd.render.renderState
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.ibukigourd.util.textRenderer
import moe.forpleuvoir.nebula.common.color.Color
import moe.forpleuvoir.nebula.common.color.Colors
import net.minecraft.CrashReport
import net.minecraft.CrashReportDetail
import net.minecraft.ReportedException
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.item.TrackingItemStackRenderState
import net.minecraft.client.renderer.state.gui.GuiItemRenderState
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import org.joml.Matrix3x2f

//region Item
fun GuiGraphicsExtractor.pushItem(
    itemStack: ItemStack,
    x: Float,
    y: Float,
    scale: ScaleFactor = ScaleFactor(1f, 1f),
    showCount: Boolean = false,
    color: Color = Colors.WHITE,
    textColor: Color = Colors.WHITE,
    seed: Int = 0
) {
    val x = x / scale.scaleX
    val y = y / scale.scaleY
    val xi = x.toInt()
    val yi = y.toInt()

    pose().pushMatrix()
    pose().scale(scale.scaleX, scale.scaleY)
    pose().translate(x - xi, y - yi)

    pushItem(mc.player, mc.level, itemStack, xi, yi, color, seed)

    if (showCount && itemStack.count > 1) {
        val count = itemStack.count.toString()
        text(textRenderer, count, xi + 16 - textRenderer.width(count), yi + textRenderer.lineHeight - 1, textColor.argb, true)
    }

    pose().popMatrix()
}

internal fun GuiGraphicsExtractor.pushItem(owner: LivingEntity?, level: Level?, itemStack: ItemStack, x: Int, y: Int, color: Color, seed: Int) {
    if (!itemStack.isEmpty) {
        val itemStackRenderState = TrackingItemStackRenderState()
        mc.itemModelResolver.updateForTopItem(itemStackRenderState, itemStack, ItemDisplayContext.GUI, level, owner, seed)
        try {
            //只是附加了 相乘的颜色
            renderState.addItem(GuiItemRenderState(Matrix3x2f(this.pose()), itemStackRenderState, x, y, peekScissorRect()).apply {
                setColor(color)
            })
        } catch (var11: Throwable) {
            val report = CrashReport.forThrowable(var11, "Rendering item")
            val category = report.addCategory("Item being rendered")
            category.setDetail("Item Type") { itemStack.item.toString() }
            category.setDetail("Item Components") { itemStack.components.toString() }
            category.setDetail("Item Foil") { itemStack.hasFoil().toString() }
            throw ReportedException(report)
        }
    }
}
//endregion