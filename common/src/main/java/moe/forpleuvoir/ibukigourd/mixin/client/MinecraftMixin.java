package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.config.ClientModConfigHandler;
import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent;
import moe.forpleuvoir.ibukigourd.event.events.client.ClientTickEvent;
import moe.forpleuvoir.ibukigourd.gui.base.tip.TipHandler;
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast;
import moe.forpleuvoir.ibukigourd.input.InputHandler;
import moe.forpleuvoir.ibukigourd.task.ClientTickTaskSchedulerKt;
import moe.forpleuvoir.ibukigourd.task.TickTaskScheduler;
import moe.forpleuvoir.nebula.event.EventBus;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("DataFlowIssue")
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    private volatile boolean running;


    @Inject(method = "run", at = @At("HEAD"))
    public void runStarting(CallbackInfo ci) {
        EventBus.Companion.broadcast(new ClientLifecycleEvent.ClientStartingEvent((Minecraft) (Object) this));
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void stop(CallbackInfo ci) {
        if (this.running) {
            EventBus.Companion.broadcast(new ClientLifecycleEvent.ClientStopEvent((Minecraft) (Object) this));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickStart(CallbackInfo ci) {
        InputHandler.INSTANCE.onTick();
        Toast.INSTANCE.onTick();
        TipHandler.INSTANCE.onTick();
        ClientTickTaskSchedulerKt.getClient(TickTaskScheduler.Companion).startTick((Minecraft) (Object) this);
        EventBus.Companion.broadcast(new ClientTickEvent.ClientTickStartEvent((Minecraft) (Object) this));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tickEnd(CallbackInfo ci) {
        ClientTickTaskSchedulerKt.getClient(TickTaskScheduler.Companion).endTick((Minecraft) (Object) this);
        EventBus.Companion.broadcast(new ClientTickEvent.ClientTickEndEvent((Minecraft) (Object) this));
    }

    @Inject(method = "resizeDisplay", at = @At("RETURN"))
    public void onResolutionChanged(CallbackInfo ci) {
        Toast.onResize();
    }

    @Inject(method = "pauseGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;hasSingleplayerServer()Z"))
    public void openGameMenu(boolean pauseOnly, CallbackInfo ci) {
        ClientModConfigHandler.INSTANCE.asyncSave();
    }
}
