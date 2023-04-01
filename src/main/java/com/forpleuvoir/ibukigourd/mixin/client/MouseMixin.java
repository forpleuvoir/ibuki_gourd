package com.forpleuvoir.ibukigourd.mixin.client;

import com.forpleuvoir.ibukigourd.input.InputHandler;
import com.forpleuvoir.ibukigourd.input.KeyCode;
import com.forpleuvoir.ibukigourd.util.NextAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {

	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	private int activeButton;

	@Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
	public void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
		if (window == this.client.getWindow().getHandle()) {
			var keyCode = KeyCode.fromCode(button);
			if (action == 1) {
				this.activeButton = button;
				if (InputHandler.onKeyPress(keyCode) == NextAction.Cancel) ci.cancel();

			} else {
				this.activeButton = -1;

			}
		}
	}

	@Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
	public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (window == this.client.getWindow().getHandle()) {

		}
	}

	@Inject(method = "onCursorPos", at = @At("HEAD"))
	public void onCursorPos(long window, double x, double y, CallbackInfo ci) {
		if (window == this.client.getWindow().getHandle()) {

		}
	}
}