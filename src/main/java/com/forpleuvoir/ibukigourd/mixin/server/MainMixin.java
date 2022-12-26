package com.forpleuvoir.ibukigourd.mixin.server;

import com.forpleuvoir.ibukigourd.event.IbukiGourdEventManager;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin {
    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;createGameVersion()V", shift = At.Shift.AFTER))
    private static void main(String[] args, CallbackInfo ci) {
        IbukiGourdEventManager.INSTANCE.init();
    }
}
