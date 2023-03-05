package com.forpleuvoir.ibukigourd.mixin.client;

import com.forpleuvoir.ibukigourd.event.events.client.input.KeyboardEvent;
import com.forpleuvoir.ibukigourd.input.InputHandler;
import com.forpleuvoir.ibukigourd.util.NextAction;
import com.forpleuvoir.nebula.event.EventBus;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.forpleuvoir.ibukigourd.input.KeyEnvironmentKt.currentEnv;
import static org.lwjgl.glfw.GLFW.*;

@Mixin(Keyboard.class)
abstract class KeyboardMixin {

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
	public void onKeyPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
		if (window == this.client.getWindow().getHandle()) {
			//key press
			if (action == GLFW_PRESS || action == GLFW_REPEAT) {
				var keyEvent = new KeyboardEvent.KeyPressEvent(key, InputUtil.fromKeyCode(key, 0).getLocalizedText().getString(), currentEnv());
				EventBus.Companion.broadcast(keyEvent);
				if (keyEvent.getCanceled()) {
					ci.cancel();
					return;
				}
				if (InputHandler.onKeyPress(key) == NextAction.Cancel) ci.cancel();
			}
			//key release
			else if (action == GLFW_RELEASE) {
				var keyEvent = new KeyboardEvent.KeyReleaseEvent(key, InputUtil.fromKeyCode(key, 0).getLocalizedText().getString(), currentEnv());
				EventBus.Companion.broadcast(keyEvent);
				if (keyEvent.getCanceled()) {
					ci.cancel();
					return;
				}
				if (InputHandler.onKeyRelease(key) == NextAction.Cancel) ci.cancel();
			}
		}

	}
}