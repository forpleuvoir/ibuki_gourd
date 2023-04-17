package com.forpleuvoir.ibukigourd.mixin.client;

import com.forpleuvoir.ibukigourd.event.events.client.input.KeyboardEvent;
import com.forpleuvoir.ibukigourd.gui.screen.ScreenManager;
import com.forpleuvoir.ibukigourd.input.InputHandler;
import com.forpleuvoir.ibukigourd.input.KeyCode;
import com.forpleuvoir.ibukigourd.util.NextAction;
import com.forpleuvoir.nebula.event.EventBus;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
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
	public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
		if (window == this.client.getWindow().getHandle()) {
			var keyCode = KeyCode.fromCode(key);
			//key press
			if (action == GLFW_PRESS || action == GLFW_REPEAT) {
				var keyEvent = new KeyboardEvent.KeyPressEvent(keyCode, keyCode.getKeyName(), currentEnv());
				EventBus.Companion.broadcast(keyEvent);
				if (keyEvent.getCanceled()) {
					ci.cancel();
					return;
				}
				if (InputHandler.onKeyPress(keyCode) == NextAction.Cancel) ci.cancel();
				ScreenManager.hasScreen(screen -> {
					if (screen.getActive()) {
						screen.getKeyPress().invoke(keyCode);
						ci.cancel();
					}
				});
			}
			//key release
			else if (action == GLFW_RELEASE) {
				var keyEvent = new KeyboardEvent.KeyReleaseEvent(keyCode, keyCode.getKeyName(), currentEnv());
				EventBus.Companion.broadcast(keyEvent);
				if (keyEvent.getCanceled()) {
					ci.cancel();
					return;
				}
				if (InputHandler.onKeyRelease(keyCode) == NextAction.Cancel) ci.cancel();
				ScreenManager.hasScreen(screen -> {
					if (screen.getActive()) {
						screen.getKeyRelease().invoke(keyCode);
						ci.cancel();
					}
				});
			}
		}
	}

	@Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
	public void onChar(long l, int i, int j, CallbackInfo ci) {
		if (l == this.client.getWindow().getHandle()) {
			if (Character.charCount(i) == 1) {
				ScreenManager.hasScreen(screen -> {
					if (screen.getActive()) {
						screen.getCharTyped().invoke((char) i);
						ci.cancel();
					}
				});
			} else {
				for (char c : Character.toChars(i)) {
					ScreenManager.hasScreen(screen -> {
						if (screen.getActive()) {
							screen.getCharTyped().invoke(c);
							ci.cancel();
						}
					});
				}
			}
		}

	}
}