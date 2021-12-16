package forpleuvoir.ibuki_gourd.mixin;

import forpleuvoir.ibuki_gourd.event.events.ClientEndTickEvent;
import forpleuvoir.ibuki_gourd.event.events.ClientStartEvent;
import forpleuvoir.ibuki_gourd.event.events.ClientStartTickEvent;
import forpleuvoir.ibuki_gourd.event.events.ClientStopEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MinecraftClient
 * <p>
 * 项目名 ibuki_gourd
 * <p>
 * 包名 forpleuvoir.ibuki_gourd.mixin
 * <p>
 * 文件名 MixinMinecraftClient
 * <p>
 * 创建时间 2021/12/9 12:51
 *
 * @author forpleuvoir
 */
@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

	/**
	 * tick start 事件发布
	 */
	@Inject(method = "tick", at = @At("HEAD"))
	private void startTick(CallbackInfo callbackInfo) {
		new ClientStartTickEvent().broadcast();
	}

	/**
	 * tick end 事件发布
	 */
	@Inject(method = "tick", at = @At("RETURN"))
	private void endTick(CallbackInfo callbackInfo) {
		new ClientEndTickEvent().broadcast();
	}

	/**
	 * 客户端关闭事件
	 */
	@Inject(at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V", shift = At.Shift.AFTER), method = "stop")
	private void onStopping(CallbackInfo callbackInfo) {
		new ClientStopEvent().broadcast();
	}

	/**
	 * 客户端启动事件
	 */
	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;thread:Ljava/lang/Thread;", shift = At.Shift.AFTER, ordinal = 0), method = "run")
	private void onStart(CallbackInfo callbackInfo) {
		new ClientStartEvent().broadcast();
	}

}
