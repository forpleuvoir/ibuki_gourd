package com.forpleuvoir.ibukigourd.mixin.common;

import com.forpleuvoir.ibukigourd.event.events.ModInitializerEvent;
import com.forpleuvoir.nebula.event.EventBus;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.impl.entrypoint.EntrypointUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(value = EntrypointUtils.class, remap = false)
public class EntrypointUtilsMixin {

    @Redirect(method = "invoke0", at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/api/entrypoint/EntrypointContainer;getEntrypoint()Ljava/lang/Object;"))
    private static <T> T modInit(EntrypointContainer<T> instance) {
        ModContainer modContainer = instance.getProvider();
        ModMetadata metadata = modContainer.getMetadata();
        EventBus.Companion.broadcast(new ModInitializerEvent(metadata));
        System.out.println("模组[" + metadata.getName() + "]初始化中");
        return instance.getEntrypoint();
    }

}
