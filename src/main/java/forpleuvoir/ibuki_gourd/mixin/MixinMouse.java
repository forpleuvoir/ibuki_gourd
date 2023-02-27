package forpleuvoir.ibuki_gourd.mixin;

import forpleuvoir.ibuki_gourd.event.events.MousePressEvent;
import forpleuvoir.ibuki_gourd.event.events.MouseReleaseEvent;
import forpleuvoir.ibuki_gourd.keyboard.KeyEnvironment;
import forpleuvoir.ibuki_gourd.keyboard.KeyboardUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 项目名 ibuki_gourd
 * <p>
 * 包名 forpleuvoir.ibuki_gourd.mixin
 * <p>
 * 文件名 MixinMouse
 * <p>
 * 创建时间 2021/12/21 20:25
 *
 * @author forpleuvoir
 */
@Mixin(Mouse.class)
public abstract class MixinMouse {

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
	public void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
		if (window == this.client.getWindow().getHandle()) {
			KeyEnvironment keyEnv = this.client.currentScreen == null || this.client.currentScreen.passEvents ? KeyEnvironment.IN_GAME : KeyEnvironment.IN_SCREEN;
			if (action == 1) {
				var event = new MousePressEvent(button, mods, keyEnv);
				event.broadcast();
				if (event.isCanceled()) ci.cancel();
				if (KeyboardUtil.setPressed(button)) ci.cancel();
			} else {
				var event = new MouseReleaseEvent(button, mods, keyEnv);
				event.broadcast();
				if (event.isCanceled()) ci.cancel();
				KeyboardUtil.setRelease(button);
			}
		}
	}
}
