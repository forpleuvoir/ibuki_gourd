package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.event.events.client.input.MouseEvent;
import moe.forpleuvoir.ibukigourd.input.InputHandler;
import moe.forpleuvoir.ibukigourd.input.KeyCode;
import moe.forpleuvoir.ibukigourd.input.MouseKt;
import moe.forpleuvoir.nebula.event.EventBus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static moe.forpleuvoir.ibukigourd.input.KeyEnvironmentKt.currentEnv;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private int activeButton;

    @Shadow
    private double glfwTime;

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    public void ibukigourd$onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window == this.client.getWindow().getHandle()) {
            final var keyCode = (moe.forpleuvoir.ibukigourd.input.Mouse) KeyCode.fromCode(button);
            if (action == 1) {
                this.activeButton = button;
                final var event = new MouseEvent.MousePressEvent(keyCode, keyCode.getKeyName(), currentEnv());
                EventBus.Companion.broadcast(event);
                if (event.getCanceled()) {
                    ci.cancel();
                    return;
                }
                if (InputHandler.onKeyPress(keyCode)) ci.cancel();
            } else {
                this.activeButton = -1;
                final var keyEvent = new MouseEvent.MouseReleaseEvent(keyCode, keyCode.getKeyName(), currentEnv());
                EventBus.Companion.broadcast(keyEvent);
                if (keyEvent.getCanceled()) {
                    ci.cancel();
                    return;
                }
                if (InputHandler.onKeyRelease(keyCode)) ci.cancel();
            }
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    public void ibukigourd$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == this.client.getWindow().getHandle()) {
            final double amount = (this.client.options.getDiscreteMouseScroll().getValue() ? Math.signum(vertical) : vertical) * this.client.options.getMouseWheelSensitivity().getValue();
            final var event = new MouseEvent.MouseScrollEvent(amount, currentEnv());
            EventBus.Companion.broadcast(event);
            if (event.getCanceled()) {
                ci.cancel();
                return;
            }
        }
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"), cancellable = true)
    public void ibukigourd$onCursorPos(long window, double x, double y, CallbackInfo ci) {
        if (window == this.client.getWindow().getHandle()) {
            final var position = MouseKt.getMousePosition(this.client);
            final var event = new MouseEvent.MouseMoveEvent(position.getX(), position.getY(), currentEnv());
            EventBus.Companion.broadcast(event);
            if (event.getCanceled()) {
                ci.cancel();
                return;
            }
            if (this.activeButton != -1 && this.glfwTime > 0.0) {
                final var keyCode = (moe.forpleuvoir.ibukigourd.input.Mouse) KeyCode.fromCode(activeButton);
                final var draggingEvent = new MouseEvent.MouseDraggingEvent(keyCode, keyCode.getKeyName(), position.getX(), position.getY(), currentEnv());
                EventBus.Companion.broadcast(draggingEvent);
                if (draggingEvent.getCanceled()) {
                    ci.cancel();
                    return;
                }
            }
        }
    }
}