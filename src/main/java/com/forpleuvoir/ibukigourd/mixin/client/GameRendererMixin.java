package com.forpleuvoir.ibukigourd.mixin.client;

import com.forpleuvoir.ibukigourd.gui.screen.ScreenManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Shadow
	@Final
	MinecraftClient client;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", ordinal = 1))
	public void renderScreen(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		ScreenManager.hasScreen(screen -> {
			if (screen.getVisible()) screen.getRender().invoke(new MatrixStack(), client.getLastFrameDuration());
		});
	}


}
