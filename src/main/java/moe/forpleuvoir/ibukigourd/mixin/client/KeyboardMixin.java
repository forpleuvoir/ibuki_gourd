package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.event.events.client.input.KeyboardEvent;
import moe.forpleuvoir.ibukigourd.gui.base.event.CharTypedEvent;
import moe.forpleuvoir.ibukigourd.gui.base.event.KeyPressEvent;
import moe.forpleuvoir.ibukigourd.gui.base.event.KeyReleaseEvent;
import moe.forpleuvoir.ibukigourd.gui.screen.ScreenManager;
import moe.forpleuvoir.ibukigourd.input.InputHandler;
import moe.forpleuvoir.ibukigourd.input.KeyCode;
import moe.forpleuvoir.nebula.event.EventBus;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static moe.forpleuvoir.ibukigourd.input.KeyEnvironmentKt.currentEnv;
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
                if (InputHandler.onKeyPress(keyCode)) ci.cancel();
                ScreenManager.hasScreen(screen -> {
                    if (screen.getActive()) {
                        screen.getKeyPress().invoke(new KeyPressEvent(keyCode));
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
                if (InputHandler.onKeyRelease(keyCode)) ci.cancel();
                ScreenManager.hasScreen(screen -> {
                    if (screen.getActive()) {
                        screen.getKeyRelease().invoke(new KeyReleaseEvent(keyCode));
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
                        screen.getCharTyped().invoke(new CharTypedEvent((char) i));
                        ci.cancel();
                    }
                });
            } else {
                for (char c : Character.toChars(i)) {
                    ScreenManager.hasScreen(screen -> {
                        if (screen.getActive()) {
                            screen.getCharTyped().invoke(new CharTypedEvent(c));
                            ci.cancel();
                        }
                    });
                }
            }
        }

    }
}