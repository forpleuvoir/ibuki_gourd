package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.event.events.client.input.KeyboardEvent;
import moe.forpleuvoir.ibukigourd.input.InputHandler;
import moe.forpleuvoir.ibukigourd.input.KeyCode;
import moe.forpleuvoir.nebula.event.EventBus;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static moe.forpleuvoir.ibukigourd.input.KeyEnvironmentKt.currentEnv;
import static org.lwjgl.glfw.GLFW.*;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    public void keyPress(long p_window, int action, KeyEvent event, CallbackInfo ci) {
        if (p_window == this.minecraft.getWindow().handle()) {
            var keyCode = KeyCode.fromCode(event.key());
            //key press
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                var keyEvent = new KeyboardEvent.KeyPressEvent(keyCode, keyCode.getKeyName(), currentEnv());
                EventBus.Companion.broadcast(keyEvent);
                if (keyEvent.getCanceled()) {
                    ci.cancel();
                    return;
                }
                if (InputHandler.onKeyPress(keyCode)) ci.cancel();
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
            }
        }
    }

}