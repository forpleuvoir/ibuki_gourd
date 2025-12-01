package moe.forpleuvoir.ibukigourd.mixin;

import moe.forpleuvoir.ibukigourd.event.events.server.ServerLifecycleEvent;
import moe.forpleuvoir.ibukigourd.event.events.server.ServerSavingEvent;
import moe.forpleuvoir.ibukigourd.task.TickTaskScheduler;
import moe.forpleuvoir.nebula.event.EventBus;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;initServer()Z"))
    private void beforeSetupServer(CallbackInfo info) {
        EventBus.Companion.broadcast(new ServerLifecycleEvent.ServerStartingEvent((MinecraftServer) (Object) this));
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;"))
    private void afterSetupServer(CallbackInfo info) {
        EventBus.Companion.broadcast(new ServerLifecycleEvent.ServerStartedEvent((MinecraftServer) (Object) this));
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void beforeShutdownServer(CallbackInfo info) {
        EventBus.Companion.broadcast(new ServerLifecycleEvent.ServerStoppingEvent((MinecraftServer) (Object) this));
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    private void afterShutdownServer(CallbackInfo info) {
        EventBus.Companion.broadcast(new ServerLifecycleEvent.ServerStoppedEvent((MinecraftServer) (Object) this));
    }

    @Inject(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;saveDataTag(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/storage/WorldData;Lnet/minecraft/nbt/CompoundTag;)V", shift = At.Shift.AFTER))
    private void saveEverything(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
        EventBus.Companion.broadcast(new ServerSavingEvent((MinecraftServer) (Object) this));
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    public void startTick(CallbackInfo ci) {
        TickTaskScheduler.getServer().startTick((MinecraftServer) (Object) this);
    }

    @Inject(method = "tickServer", at = @At("RETURN"))
    public void endTick(CallbackInfo ci) {
        TickTaskScheduler.getServer().endTick((MinecraftServer) (Object) this);
    }

}
