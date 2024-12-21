package moe.forpleuvoir.ibukigourd.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext;
import moe.forpleuvoir.ibukigourd.gui.base.tip.TipHandler;
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    public abstract void tick();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;draw()V", ordinal = 1, shift = At.Shift.AFTER))
    public void ibukigourd$render(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci, @Local(ordinal = 0) DrawContext context, @Local(ordinal = 0) int mouseX, @Local(ordinal = 1) int mouseY) {
        var ctx = IGDrawContext.Companion.toIGDrawContext(context);
        TipHandler.render(ctx, mouseX, mouseY, tickCounter.getLastFrameDuration());
        Toast.render(ctx, mouseX, mouseY, tickCounter.getLastFrameDuration());
    }

}
