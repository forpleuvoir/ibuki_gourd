package moe.forpleuvoir.ibukigourd.mixin.client;

import com.mojang.blaze3d.platform.Window;
import moe.forpleuvoir.ibukigourd.ui.overlay.OverlayHost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public abstract class WindowMixin {

    @Inject(method = "onFramebufferResize", at = @At("TAIL"))
    private void onFramebufferResize(long handle, int newWidth, int newHeight, CallbackInfo ci) {
        OverlayHost.INSTANCE.onWindowResized();
    }

    @Inject(method = "setGuiScale", at = @At("TAIL"))
    private void onSetGuiScale(int guiScale, CallbackInfo ci) {
        OverlayHost.INSTANCE.onWindowResized();
    }
}
