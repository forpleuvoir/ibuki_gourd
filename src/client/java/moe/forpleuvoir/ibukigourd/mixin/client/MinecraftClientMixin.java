package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.event.IbukiGourdEventManager;
import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent;
import moe.forpleuvoir.ibukigourd.event.events.client.ClientTickEvent;
import moe.forpleuvoir.ibukigourd.gui.base.tip.HoverTipHandler;
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast;
import moe.forpleuvoir.ibukigourd.input.InputHandler;
import moe.forpleuvoir.ibukigourd.task.ClientTickTaskSchedulerKt;
import moe.forpleuvoir.ibukigourd.task.TickTaskScheduler;
import moe.forpleuvoir.nebula.event.EventBus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("DataFlowIssue")
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    private volatile boolean running;


    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/System;currentTimeMillis()J"))
    private void ibukigourd$init(RunArgs args, CallbackInfo ci) {
        IbukiGourdEventManager.INSTANCE.init();
    }

    @Inject(method = "run", at = @At("HEAD"))
    public void ibukigourd$runStarting(CallbackInfo ci) {
        EventBus.Companion.broadcast(new ClientLifecycleEvent.ClientStartingEvent((MinecraftClient) (Object) this));
    }

    @Inject(method = "run", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;thread:Ljava/lang/Thread;", shift = At.Shift.AFTER, ordinal = 0))
    public void ibukigourd$runStarted(CallbackInfo ci) {
        EventBus.Companion.broadcast(new ClientLifecycleEvent.ClientStartedEvent((MinecraftClient) (Object) this));
    }

    @Inject(method = "scheduleStop", at = @At("HEAD"))
    private void ibukigourd$onStopping(CallbackInfo ci) {
        if (this.running) {
            EventBus.Companion.broadcast(new ClientLifecycleEvent.ClientStopEvent((MinecraftClient) (Object) this));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void ibukigourd$tickStart(CallbackInfo ci) {
        InputHandler.INSTANCE.onTick();
        Toast.INSTANCE.onTick();
        HoverTipHandler.INSTANCE.onTick();
        ClientTickTaskSchedulerKt.getClient(TickTaskScheduler.Companion).startTick((MinecraftClient) (Object) this);
        EventBus.Companion.broadcast(new ClientTickEvent.ClientTickStartEvent((MinecraftClient) (Object) this));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void ibukigourd$tickEnd(CallbackInfo ci) {
        ClientTickTaskSchedulerKt.getClient(TickTaskScheduler.Companion).endTick((MinecraftClient) (Object) this);
        EventBus.Companion.broadcast(new ClientTickEvent.ClientTickEndEvent((MinecraftClient) (Object) this));
    }

    @Inject(method = "onResolutionChanged", at = @At("RETURN"))
    public void ibukigourd$onResolutionChanged(CallbackInfo ci) {
        Toast.onResize();
    }
}
