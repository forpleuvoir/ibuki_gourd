package moe.forpleuvoir.ibukigourd.mixin.server;

import com.mojang.datafixers.DataFixer;
import moe.forpleuvoir.ibukigourd.IbukiGourd;
import moe.forpleuvoir.ibukigourd.event.events.server.ServerLifecycleEvent;
import moe.forpleuvoir.ibukigourd.event.events.server.ServerSavingEvent;
import moe.forpleuvoir.ibukigourd.util.ModLogger;
import moe.forpleuvoir.nebula.event.EventBus;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.ApiServices;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

	@Unique
	private static final ModLogger log = new ModLogger(MinecraftServerMixin.class, IbukiGourd.MOD_NAME);

	@Inject(method = "<init>", at = @At("RETURN"))
	public void ibukigourd$init(
			Thread serverThread,
			LevelStorage.Session session,
			ResourcePackManager dataPackManager,
			SaveLoader saveLoader,
			Proxy proxy,
			DataFixer dataFixer,
			ApiServices apiServices,
			WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory,
			CallbackInfo ci) {
		log.info("server initialized");
	}

	@Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"))
	private void ibukigourd$beforeSetupServer(CallbackInfo info) {
		EventBus.Companion.broadcast(new ServerLifecycleEvent.ServerStartingEvent((MinecraftServer) (Object) this));
	}

	@Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;createMetadata()Lnet/minecraft/server/ServerMetadata;"))
	private void ibukigourd$afterSetupServer(CallbackInfo info) {
		EventBus.Companion.broadcast(new ServerLifecycleEvent.ServerStartedEvent((MinecraftServer) (Object) this));
	}

	@Inject(method = "shutdown", at = @At("HEAD"))
	private void ibukigourd$beforeShutdownServer(CallbackInfo info) {
		EventBus.Companion.broadcast(new ServerLifecycleEvent.ServerStoppingEvent((MinecraftServer) (Object) this));
	}

	@Inject(method = "shutdown", at = @At("TAIL"))
	private void ibukigourd$afterShutdownServer(CallbackInfo info) {
		EventBus.Companion.broadcast(new ServerLifecycleEvent.ServerStoppedEvent((MinecraftServer) (Object) this));
	}

	@Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getOverworld()Lnet/minecraft/server/world/ServerWorld;", shift = At.Shift.AFTER))
	private void ibukigourd$saveEverything(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
		EventBus.Companion.broadcast(new ServerSavingEvent((MinecraftServer) (Object) this));
	}
}
