package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.event.events.client.input.KeyboardEvent;
import moe.forpleuvoir.ibukigourd.input.InputHandler;
import moe.forpleuvoir.ibukigourd.input.KeyCode;
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
    public void keyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (handle == this.minecraft.getWindow().handle()) {
            var keyCode = KeyCode.fromCode(event.key());
            //key press
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                final var context = new KeyboardEvent.KeyboardContext(
                        keyCode,
                        event.modifiers(),
                        true,
                        action == GLFW_REPEAT,
                        keyCode.getKeyName(),
                        currentEnv()
                );
                KeyboardEvent.Pressed.invoker().invoke(context);
                if (context.isCancelled()) {
                    ci.cancel();
                    return;
                }
                if (!InputHandler.onKeyPress(keyCode)) ci.cancel();
            }
            //key release
            else if (action == GLFW_RELEASE) {
                final var context = new KeyboardEvent.KeyboardContext(
                        keyCode,
                        event.modifiers(),
                        false, false,
                        keyCode.getKeyName(),
                        currentEnv()
                );
                KeyboardEvent.Released.invoker().invoke(context);
                if (context.isCancelled()) {
                    ci.cancel();
                    return;
                }
                if (!InputHandler.onKeyRelease(keyCode)) ci.cancel();
            }
        }
    }

}