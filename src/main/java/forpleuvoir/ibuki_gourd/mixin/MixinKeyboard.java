package forpleuvoir.ibuki_gourd.mixin;


import forpleuvoir.ibuki_gourd.event.events.KeyPressEvent;
import forpleuvoir.ibuki_gourd.event.events.KeyReleaseEvent;
import forpleuvoir.ibuki_gourd.event.util.KeyEnvironment;
import forpleuvoir.ibuki_gourd.keyboard.KeyboardUtil;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MinecraftClient
 * <p>
 * 项目名 ibuki_gourd
 * <p>
 * 包名 forpleuvoir.ibuki_gourd.mixin
 * <p>
 * 文件名 MixinKeyboard
 * <p>
 * 创建时间 2021/12/17 17:42
 *
 * @author forpleuvoir
 */
@Mixin(Keyboard.class)
public class MixinKeyboard {

	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	private boolean repeatEvents;

	@Inject(method = "onKey", at = @At("HEAD"))
	public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
		if (window == this.client.getWindow().getHandle()) {
			KeyEnvironment keyEnv = this.client.currentScreen == null ? KeyEnvironment.IN_GAME : KeyEnvironment.IN_SCREEN;
			if (action == 1 || action == 2 && this.repeatEvents) {
				KeyboardUtil.setPressed(key);
				new KeyPressEvent(key, scancode, modifiers, keyEnv).broadcast();
			} else {
				KeyboardUtil.setRelease(key);
				new KeyReleaseEvent(key, scancode, modifiers, keyEnv).broadcast();
			}
		}
	}
}
