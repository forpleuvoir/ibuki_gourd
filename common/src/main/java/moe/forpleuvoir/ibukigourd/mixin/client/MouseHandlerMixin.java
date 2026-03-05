package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.event.events.client.input.MouseEvent;
import moe.forpleuvoir.ibukigourd.input.InputHandler;
import moe.forpleuvoir.ibukigourd.input.KeyCode;
import moe.forpleuvoir.ibukigourd.input.MouseKt;
import moe.forpleuvoir.nebula.event.EventBus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static moe.forpleuvoir.ibukigourd.input.KeyEnvironmentKt.currentEnv;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private double mousePressedTime;

    @Shadow
    private int activeButton;

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    public void onPress(long windowPointer, int button, int action, int modifiers, CallbackInfo ci) {
        if (windowPointer == this.minecraft.getWindow().getWindow()) {
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
                final var event = new MouseEvent.MouseReleaseEvent(keyCode, keyCode.getKeyName(), currentEnv());
                EventBus.Companion.broadcast(event);
                if (event.getCanceled()) {
                    ci.cancel();
                    return;
                }
                if (InputHandler.onKeyRelease(keyCode)) ci.cancel();
            }
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    public void onMouseScroll(long windowPointer, double horizontal, double vertical, CallbackInfo ci) {
        if (windowPointer == this.minecraft.getWindow().getWindow()) {
            final double amount = (this.minecraft.options.discreteMouseScroll().get() ? Math.signum(vertical) : vertical) * this.minecraft.options.mouseWheelSensitivity().get();
            final var event = new MouseEvent.MouseScrollEvent(amount, currentEnv());
            EventBus.Companion.broadcast(event);
            if (event.getCanceled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
    public void onMove(long windowPointer, double xpos, double ypos, CallbackInfo ci) {
        if (windowPointer == this.minecraft.getWindow().getWindow()) {
            final var position = MouseKt.getMousePosition(this.minecraft);
            final var event = new MouseEvent.MouseMoveEvent(position.getX(), position.getY(), currentEnv());
            EventBus.Companion.broadcast(event);
            if (event.getCanceled()) {
                ci.cancel();
                return;
            }
            if (this.activeButton != -1 && this.mousePressedTime > 0.0) {
                final var keyCode = (moe.forpleuvoir.ibukigourd.input.Mouse) KeyCode.fromCode(activeButton);
                final var draggingEvent = new MouseEvent.MouseDraggingEvent(keyCode, keyCode.getKeyName(), position.getX(), position.getY(), currentEnv());
                EventBus.Companion.broadcast(draggingEvent);
                if (draggingEvent.getCanceled()) {
                    ci.cancel();
                }
            }
        }
    }
}