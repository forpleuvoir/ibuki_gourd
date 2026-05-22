package moe.forpleuvoir.ibukigourd.mixin;

import moe.forpleuvoir.ibukigourd.event.events.server.ServerLifecycleEvent;
import moe.forpleuvoir.ibukigourd.task.TickTaskScheduler;
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
        ServerLifecycleEvent.Starting.invoker().invoke((MinecraftServer) (Object) this);
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;"))
    private void afterSetupServer(CallbackInfo info) {
        ServerLifecycleEvent.Started.invoker().invoke((MinecraftServer) (Object) this);
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void beforeShutdownServer(CallbackInfo info) {
        ServerLifecycleEvent.Stopping.invoker().invoke((MinecraftServer) (Object) this);
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    private void afterShutdownServer(CallbackInfo info) {
        ServerLifecycleEvent.Stopped.invoker().invoke((MinecraftServer) (Object) this);
    }

    @Inject(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;saveDataTag(Lnet/minecraft/world/level/storage/WorldData;Ljava/util/UUID;)V", shift = At.Shift.AFTER))
    private void saveEverything(boolean silent, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        ServerLifecycleEvent.Saving.invoker().invoke((MinecraftServer) (Object) this);
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
