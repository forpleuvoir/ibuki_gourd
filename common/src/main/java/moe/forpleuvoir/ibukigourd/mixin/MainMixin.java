package moe.forpleuvoir.ibukigourd.mixin;

import moe.forpleuvoir.ibukigourd.event.IbukiGourdEventManager;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public abstract class MainMixin {
    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;tryDetectVersion()V", shift = At.Shift.AFTER))
    private static void init(String[] args, CallbackInfo ci) {
        IbukiGourdEventManager.INSTANCE.init();
    }
}
