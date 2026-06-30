package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.render.extension.state.ItemRenderStateExtension;
import net.minecraft.client.renderer.state.gui.pip.OversizedItemRenderState;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(OversizedItemRenderState.class)
@Implements(@Interface(iface = ItemRenderStateExtension.class, prefix = "ibukigourd$"))
public class OversizedItemRenderStateMixin {
    @Unique
    private int ibukigourd$color = -1;

    @Unique
    public int ibukigourd$getColor() {
        return ibukigourd$color;
    }

    @Unique
    public void ibukigourd$setColor(int color) {
        ibukigourd$color = color;
    }
}
