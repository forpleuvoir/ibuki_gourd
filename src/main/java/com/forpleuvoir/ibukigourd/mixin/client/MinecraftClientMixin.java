package com.forpleuvoir.ibukigourd.mixin.client;

import com.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent;
import com.forpleuvoir.ibukigourd.event.events.client.ClientTickEvent;
import com.forpleuvoir.ibukigourd.gui.screen.ScreenManager;
import com.forpleuvoir.ibukigourd.input.InputHandler;
import com.forpleuvoir.nebula.event.EventBus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {


	@Shadow
	private volatile boolean running;

	@Shadow
	@Final
	private Window window;

	@Inject(method = "run", at = @At("HEAD"))
	public void runStarting(CallbackInfo ci) {
		EventBus.Companion.broadcast(new ClientLifecycleEvent.ClientStartingEvent((MinecraftClient) (Object) this));
	}

	@Inject(method = "run", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;thread:Ljava/lang/Thread;", shift = At.Shift.AFTER, ordinal = 0))
	public void runStarted(CallbackInfo ci) {
		EventBus.Companion.broadcast(new ClientLifecycleEvent.ClientStartedEvent((MinecraftClient) (Object) this));
	}

	@Inject(method = "scheduleStop", at = @At("HEAD"))
	private void onStopping(CallbackInfo ci) {
		if (this.running) {
			EventBus.Companion.broadcast(new ClientLifecycleEvent.ClientStopEvent((MinecraftClient) (Object) this));
		}
	}

	@Inject(method = "onResolutionChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setScaleFactor(D)V", shift = At.Shift.AFTER))
	public void screenResize(CallbackInfo ci) {
		ScreenManager.hasScreen(screen -> screen.getResize().invoke(this.window.getScaledWidth(), this.window.getScaledHeight()));
	}

	@Inject(method = "openPauseMenu", at = @At(value = "HEAD"), cancellable = true)
	public void openPauseMenu(CallbackInfo ci) {
		if (ScreenManager.hasScreen()) ci.cancel();
	}

	@Inject(method = "setScreen", at = @At(value = "HEAD"), cancellable = true)
	public void setScreen(CallbackInfo ci) {
		if (ScreenManager.hasScreen()) ci.cancel();
	}

//	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;shouldPause()Z", shift = At.Shift.AFTER))
//	public boolean pauseGame(boolean original) {
//		AtomicBoolean returnValue = new AtomicBoolean(original);
//		ScreenManager.hasScreen(screen -> {
//			returnValue.set(original || screen.getPauseGame());
//		});
//		return returnValue.get();
//	}

	@Inject(method = "tick", at = @At("HEAD"))
	public void tickStart(CallbackInfo ci) {
		EventBus.Companion.broadcast(new ClientTickEvent.ClientTickStartEvent((MinecraftClient) (Object) this));
		InputHandler.INSTANCE.tick();
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundManager;tick(Z)V", shift = At.Shift.BEFORE))
	public void screenTick(CallbackInfo ci) {
		ScreenManager.INSTANCE.tick();
	}

	@Inject(method = "tick", at = @At("RETURN"))
	public void tickEnd(CallbackInfo ci) {
		EventBus.Companion.broadcast(new ClientTickEvent.ClientTickEndEvent((MinecraftClient) (Object) this));
	}

}
