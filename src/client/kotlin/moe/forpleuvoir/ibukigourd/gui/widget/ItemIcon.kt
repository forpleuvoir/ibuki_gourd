package moe.forpleuvoir.ibukigourd.gui.widget

import com.mojang.blaze3d.systems.RenderSystem
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontext.useMatrixStack
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.attachLeft
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.render
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.size
import moe.forpleuvoir.ibukigourd.gui.base.scope.WidgetContainerScope
import moe.forpleuvoir.ibukigourd.render.defaultZOffset
import moe.forpleuvoir.ibukigourd.util.math.Vector3f
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateBy
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.item.ModelTransformationMode
import net.minecraft.world.World

@JvmName("ItemStackIcon")
fun WidgetContainerScope.ItemIcon(
    item: State<ItemStack>,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) = Widget(modifier.attachLeft {
    size(16f * scale, 16f * scale)
        .render { context, x, y, delta ->
            context.renderItem(item.getValue(), transform.worldX, transform.worldY, defaultZOffset, scale)
        }
})

fun WidgetContainerScope.ItemIcon(
    item: ItemStack,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) = ItemIcon(stateOf(item), scale, modifier)


fun WidgetContainerScope.ItemIcon(
    item: ItemConvertible,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) = ItemIcon(stateOf(ItemStack(item)), scale, modifier)

fun WidgetContainerScope.ItemIcon(
    item: State<out ItemConvertible>,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) = ItemIcon(mutableStateBy { ItemStack(item.getValue()) }, scale, modifier)

private val NORMALIZE_A = Vector3f(-0.5F, -1.0F, 0.0F).normalize()
private val NORMALIZE_B = Vector3f(0.5F, -1.0F, 1.0F).normalize()

fun DrawContext.renderItem(
    stack: ItemStack,
    x: Float,
    y: Float,
    z: Float = defaultZOffset,
    scale: Float = 1f,
    seed: Int = 0,
    entity: LivingEntity? = this.client.player,
    world: World? = this.client.world
) {
    if (stack.isEmpty || scale == 0f) return
    this.client.itemModelManager.update(this.itemRenderState, stack, ModelTransformationMode.GUI, false, world, entity, seed)
    this.useMatrixStack { matrices ->
        val s = 1f / scale
        val offset = 8 * scale
        matrices.scale(16.0f * scale, -16.0f * scale, 16f * scale)
        matrices.translate((x + offset) * s / 16f, (y + offset) * s / -16f, (z + offset) * s / 16f)
        val isSideLit: Boolean = !this.itemRenderState.isSideLit
        if (isSideLit) {
            this.draw()
            DiffuseLighting.disableGuiDepthLighting()
        } else {
            RenderSystem.setShaderLights(NORMALIZE_A, NORMALIZE_B)
        }
        this.itemRenderState.render(this.matrices, this.vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV)
        this.draw()
        DiffuseLighting.enableGuiDepthLighting()
    }
}