package moe.forpleuvoir.ibukigourd.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics;
import moe.forpleuvoir.ibukigourd.gui.base.tip.TipHandler;
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeStorage;endFrame()V", ordinal = 0, shift = At.Shift.AFTER))
    public void render(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci, @Local GuiGraphics guiGraphics, @Local(ordinal = 0) int mouseX, @Local(ordinal = 1) int mouseY) {
        var ctx = IGGuiGraphics.Companion.toIGGUIGraphics(guiGraphics);
        TipHandler.render(ctx, mouseX, mouseY, deltaTracker.getGameTimeDeltaTicks());
        Toast.render(ctx, mouseX, mouseY, deltaTracker.getGameTimeDeltaTicks());
    }

}
