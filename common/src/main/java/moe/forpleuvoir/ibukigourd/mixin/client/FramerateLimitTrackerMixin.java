package moe.forpleuvoir.ibukigourd.mixin.client;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import moe.forpleuvoir.ibukigourd.ui.ComposeScreenHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mojang.blaze3d.platform.FramerateLimitTracker.FramerateThrottleReason.NONE;
import static com.mojang.blaze3d.platform.FramerateLimitTracker.FramerateThrottleReason.OUT_OF_LEVEL_MENU;

/**
 * 解除 Compose 屏幕的帧数限制。
 * <p>
 * 无世界（标题界面 / 世界加载中）且开着界面时，原版把帧数压到 60（{@code OUT_OF_LEVEL_MENU}），
 * 像素动画会明显发顿；开着 Compose 屏幕时改判为 {@code NONE}，按游戏自身的帧数上限跑。
 * 窗口最小化、挂机等其它档位不受影响。开关见 {@code IGConfig.Gui.Screen.unlimitFramerate}。
 */
@Mixin(FramerateLimitTracker.class)
public class FramerateLimitTrackerMixin {

    @Inject(method = "getThrottleReason", at = @At("RETURN"), cancellable = true)
    private void ibukigourd$unlimitComposeScreenFramerate(CallbackInfoReturnable<FramerateLimitTracker.FramerateThrottleReason> cir) {
        if (cir.getReturnValue() == OUT_OF_LEVEL_MENU && ComposeScreenHelper.shouldUnlimitFramerate()) {
            cir.setReturnValue(NONE);
        }
    }
}
