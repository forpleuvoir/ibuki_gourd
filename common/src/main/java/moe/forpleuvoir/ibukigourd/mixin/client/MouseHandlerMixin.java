package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.event.events.client.input.MouseEvent;
import moe.forpleuvoir.ibukigourd.input.InputHandler;
import moe.forpleuvoir.ibukigourd.input.KeyCode;
import moe.forpleuvoir.ibukigourd.input.MouseButtonKt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
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
    private MouseButtonInfo activeButton;

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    public void onMouseButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (handle == this.minecraft.getWindow().handle()) {
            final var keyCode = (moe.forpleuvoir.ibukigourd.input.MouseButton) KeyCode.fromCode(rawButtonInfo.button());
            if (action == 1) {
                this.activeButton = rawButtonInfo;
                final var context = new MouseEvent.MouseKeyContext(
                        keyCode,
                        rawButtonInfo.modifiers(),
                        true,
                        keyCode.getKeyName(),
                        currentEnv()
                );
                MouseEvent.Pressed.invoker().invoke(context);
                if (context.isCancelled()) {
                    ci.cancel();
                    return;
                }
                if (InputHandler.onKeyPress(keyCode)) ci.cancel();
            } else {
                this.activeButton = new MouseButtonInfo(-1, 0);
                final var context = new MouseEvent.MouseKeyContext(
                        keyCode,
                        rawButtonInfo.modifiers(),
                        true,
                        keyCode.getKeyName(),
                        currentEnv()
                );
                MouseEvent.Released.invoker().invoke(context);
                if (context.isCancelled()) {
                    ci.cancel();
                    return;
                }
                if (InputHandler.onKeyRelease(keyCode)) ci.cancel();
            }
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    public void onMouseScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        if (handle == this.minecraft.getWindow().handle()) {
            boolean discreteScroll = this.minecraft.options.discreteMouseScroll().get();
            double scrollSensitivity = this.minecraft.options.mouseWheelSensitivity().get();
            double scaledXOffset = (discreteScroll ? Math.signum(xoffset) : xoffset) * scrollSensitivity;
            double scaledYOffset = (discreteScroll ? Math.signum(yoffset) : yoffset) * scrollSensitivity;
            final var context = new MouseEvent.ScrollingContext(scaledXOffset, scaledYOffset, currentEnv());
            MouseEvent.Scrolling.invoker().invoke(context);
            if (context.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
    public void onMove(long handle, double xpos, double ypos, CallbackInfo ci) {
        if (handle == this.minecraft.getWindow().handle()) {
            final var position = MouseButtonKt.getMousePosition(this.minecraft);
            final var movingContext = new MouseEvent.MovingContext(position.getX(), position.getY(), currentEnv());
            MouseEvent.Moving.invoker().invoke(movingContext);
            if (movingContext.isCancelled()) {
                ci.cancel();
                return;
            }
            if (this.activeButton != null && this.activeButton.button() != -1 && this.mousePressedTime > 0.0) {
                final var keyCode = (moe.forpleuvoir.ibukigourd.input.MouseButton) KeyCode.fromCode(activeButton.button());
                final var draggingContext = new MouseEvent.DraggingContext(keyCode, keyCode.getKeyName(), position.getX(), position.getY(), currentEnv());
                MouseEvent.Dragging.invoker().invoke(draggingContext);
                if (draggingContext.isCancelled()) {
                    ci.cancel();
                }
            }
        }
    }
}