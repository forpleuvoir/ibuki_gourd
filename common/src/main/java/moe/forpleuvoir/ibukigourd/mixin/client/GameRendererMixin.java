package moe.forpleuvoir.ibukigourd.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import moe.forpleuvoir.ibukigourd.ui.toast.ToastOverlayHost;
import moe.forpleuvoir.ibukigourd.ui.util.ShouldBlockLevelRender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "extractGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractDeferredSubtitles()V", ordinal = 0, shift = At.Shift.AFTER))
    private void extractGui(DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo ci, @Local(name = "graphics") GuiGraphicsExtractor graphics, @Local(name = "xMouse") int xMouse, @Local(name = "yMouse") int yMouse) {
        ToastOverlayHost.INSTANCE.render(graphics, xMouse, yMouse, deltaTracker.getGameTimeDeltaPartialTick(shouldRenderLevel));
    }

    @ModifyVariable(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlobalSettingsUniform;update(IIDJLnet/minecraft/client/DeltaTracker;ILnet/minecraft/world/phys/Vec3;Z)V"),
            name = "shouldRenderLevel"
    )
    private boolean modifyRenderLevel(boolean shouldRenderLevel) {
        return !ShouldBlockLevelRender.shouldBlock() && shouldRenderLevel;
    }

}
