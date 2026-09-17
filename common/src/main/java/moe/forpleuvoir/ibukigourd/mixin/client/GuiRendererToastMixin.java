package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.ui.sokitsu.toast.ToastHost;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 提示常驻宿主的帧钩子。
 * <p>
 * 注入点选在 {@code GuiRenderer#render} 的尾部（TAIL）：此时原版 GUI（HUD / 界面 / 原版 toast）
 * 与 compose-minecraft 的 Compose 屏幕内容（它注入在 {@code draw()V} 调用前后）都已提交，
 * 提示因此落在**最上层**，且无论当前是否打开了 Compose 屏幕都会渲染。
 * <p>
 * 注入点不依赖原版 {@code draws} 是否为空，故任何界面状态下都必然触发；
 * 无活动提示时 {@code ToastHost.onFrame()} 内部直接返回，注入等价于 no-op。
 */
@Mixin(GuiRenderer.class)
public abstract class GuiRendererToastMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void ibukigourd$renderToastOverlay(CallbackInfo ci) {
        ToastHost.INSTANCE.onFrame();
    }
}
