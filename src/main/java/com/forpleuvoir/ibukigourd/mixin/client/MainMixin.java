package com.forpleuvoir.ibukigourd.mixin.client;

import com.forpleuvoir.ibukigourd.event.IbukiGourdEventManager;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin {

    @Inject(method = "main([Ljava/lang/String;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;createGameVersion()V", shift = At.Shift.AFTER))
    private static void main(String[] args, boolean optimizeDataFixer, CallbackInfo ci) {
        IbukiGourdEventManager.INSTANCE.init();
    }

}
