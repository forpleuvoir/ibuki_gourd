package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.attachLeft
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.render
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.size
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.util.math.Vector3f
import moe.forpleuvoir.ibukigourd.util.state.State
import moe.forpleuvoir.ibukigourd.util.state.mutableStateBy
import moe.forpleuvoir.ibukigourd.util.state.stateOf
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike

@JvmName("ItemStackIcon")
fun ContainerScope.ItemIcon(
    item: State<ItemStack>,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) = Widget(modifier.attachLeft {
    size(16f * scale, 16f * scale)
        .render { guiGraphics, _, _, _ ->
            guiGraphics.pushItem(item.getValue(), transform.worldX, transform.worldY, scale)
        }
})

fun ContainerScope.ItemIcon(
    item: ItemStack,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) = ItemIcon(stateOf(item), scale, modifier)


fun ContainerScope.ItemIcon(
    item: ItemLike,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) = ItemIcon(stateOf(ItemStack(item)), scale, modifier)

fun ContainerScope.ItemIcon(
    item: State<out ItemLike>,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) = ItemIcon(mutableStateBy { ItemStack(item.getValue()) }, scale, modifier)

private val NORMALIZE_A = Vector3f(-0.5F, -1.0F, 0.0F).normalize()
private val NORMALIZE_B = Vector3f(0.5F, -1.0F, 1.0F).normalize()

//fun IGGuiGraphics.renderItem(
//    stack: ItemStack,
//    x: Float,
//    y: Float,
//    z: Float = defaultZOffset,
//    scale: Float = 1f,
//    seed: Int = 0,
//    entity: LivingEntity? = this.minecraft.player,
//    world: Level? = this.minecraft.level
//) {
//    if (stack.isEmpty || scale == 0f) return
////    this.minecraft.itemModelManager.update(this.itemRenderState, stack, ModelTransformationMode.GUI, false, world, entity, seed)
////    this.useMatrix3x2 { pose ->
////        val s = 1f / scale
////        val offset = 8 * scale
////        pose.scale(16.0f * scale, -16.0f * scale, 16f * scale)
////        pose.translate((x + offset) * s / 16f, (y + offset) * s / -16f, (z + offset) * s / 16f)
////        val isSideLit: Boolean = !this.itemRenderState.isSideLit
////        if (isSideLit) {
////            this.draw()
////            DiffuseLighting.disableGuiDepthLighting()
////        } else {
////            RenderSystem.setShaderLights(NORMALIZE_A, NORMALIZE_B)
////        }
////        this.itemRenderState.render(this.matrices, this.vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV)
////        this.draw()
////        DiffuseLighting.enableGuiDepthLighting()
////    }
//}