package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.event.events.client.input.MouseEvent;
import moe.forpleuvoir.ibukigourd.gui.screen.ScreenManager;
import moe.forpleuvoir.ibukigourd.input.InputHandler;
import moe.forpleuvoir.ibukigourd.input.KeyCode;
import moe.forpleuvoir.ibukigourd.input.MouseKt;
import moe.forpleuvoir.ibukigourd.util.NextAction;
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

    @Shadow
    private double x;


    @Shadow
    private double y;

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    public void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window == this.client.getWindow().getHandle()) {
            var keyCode = (moe.forpleuvoir.ibukigourd.input.Mouse) KeyCode.fromCode(button);
            if (action == 1) {
                this.activeButton = button;
                var event = new MouseEvent.MousePressEvent(keyCode, keyCode.getKeyName(), currentEnv());
                EventBus.Companion.broadcast(event);
                if (event.getCanceled()) {
                    ci.cancel();
                    return;
                }
                if (InputHandler.onKeyPress(keyCode) == NextAction.Cancel) ci.cancel();
                ScreenManager.hasScreen(screen -> {
                    var position = MouseKt.getMousePosition(this.client);
                    screen.getMouseClick().invoke(position.getX(), position.getY(), keyCode);
                    ci.cancel();
                });
            } else {
                this.activeButton = -1;
                var keyEvent = new MouseEvent.MouseReleaseEvent(keyCode, keyCode.getKeyName(), currentEnv());
                EventBus.Companion.broadcast(keyEvent);
                if (keyEvent.getCanceled()) {
                    ci.cancel();
                    return;
                }
                if (InputHandler.onKeyRelease(keyCode) == NextAction.Cancel) ci.cancel();
                ScreenManager.hasScreen(screen -> {
                    var position = MouseKt.getMousePosition(this.client);
                    screen.getMouseRelease().invoke(position.getX(), position.getY(), keyCode);
                    ci.cancel();
                });
            }
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == this.client.getWindow().getHandle()) {
            double amount = (this.client.options.getDiscreteMouseScroll().getValue() ? Math.signum(vertical) : vertical) * this.client.options.getMouseWheelSensitivity().getValue();
            var event = new MouseEvent.MouseScrollEvent(amount, currentEnv());
            EventBus.Companion.broadcast(event);
            if (event.getCanceled()) {
                ci.cancel();
                return;
            }
            ScreenManager.hasScreen(screen -> {
                var position = MouseKt.getMousePosition(this.client);
                screen.getMouseScrolling().invoke(position.getX(), position.getY(), (float) amount);
                ci.cancel();
            });
        }
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"), cancellable = true)
    public void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        if (window == this.client.getWindow().getHandle()) {
            var position = MouseKt.getMousePosition(this.client);
            var event = new MouseEvent.MouseMoveEvent(position.getX(), position.getY(), currentEnv());
            EventBus.Companion.broadcast(event);
            if (event.getCanceled()) {
                ci.cancel();
                return;
            }
            ScreenManager.hasScreen(screen -> screen.getMouseMove().invoke(position.getX(), position.getY()));
            if (this.activeButton != -1 && this.glfwTime > 0.0) {
                var keyCode = (moe.forpleuvoir.ibukigourd.input.Mouse) KeyCode.fromCode(activeButton);
                var draggingEvent = new MouseEvent.MouseDraggingEvent(keyCode, keyCode.getKeyName(), position.getX(), position.getY(), currentEnv());
                EventBus.Companion.broadcast(draggingEvent);
                if (draggingEvent.getCanceled()) {
                    ci.cancel();
                    return;
                }
                double deltaX = (x - this.x) * (double) this.client.getWindow().getScaledWidth() / (double) this.client.getWindow().getWidth();
                double deltaY = (y - this.y) * (double) this.client.getWindow().getScaledHeight() / (double) this.client.getWindow().getHeight();

                ScreenManager.hasScreen(screen -> screen.getMouseDragging().invoke(position.getX(), position.getY(), keyCode, (float) deltaX, (float) deltaY));
            }
        }
    }
}