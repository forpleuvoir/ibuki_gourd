package moe.forpleuvoir.ibukigourd.mixin.client;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import moe.forpleuvoir.ibukigourd.ui.util.ComposeScreenHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mojang.blaze3d.platform.FramerateLimitTracker.FramerateThrottleReason.NONE;
import static com.mojang.blaze3d.platform.FramerateLimitTracker.FramerateThrottleReason.OUT_OF_LEVEL_MENU;

@Mixin(FramerateLimitTracker.class)
public class FramerateLimitTrackerMixin {

    /**
     * 解除ComposeScreen的帧数限制,使用游戏默认的帧数限制,防止在标题界面帧数被限制在60
     */
    @Inject(method = "getThrottleReason", at = @At("RETURN"), cancellable = true)
    public void getThrottleReason(CallbackInfoReturnable<FramerateLimitTracker.FramerateThrottleReason> cir) {
        if (cir.getReturnValue() == OUT_OF_LEVEL_MENU && ComposeScreenHelper.isComposeScreen()) {
            cir.setReturnValue(NONE);
        }
    }

}
