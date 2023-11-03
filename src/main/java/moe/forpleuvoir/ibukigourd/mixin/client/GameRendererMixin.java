package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.gui.screen.ScreenManager;
import moe.forpleuvoir.ibukigourd.gui.screen.TestScreenKt;
import moe.forpleuvoir.ibukigourd.render.RenderContext;
import moe.forpleuvoir.ibukigourd.render.ScissorStack;
import moe.forpleuvoir.ibukigourd.util.MiscKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    MinecraftClient client;

    @Unique
    private static final RenderContext context = new RenderContext(MinecraftClient.getInstance(), new MatrixStack(), new ScissorStack());

    @Unique
    private static final List<Long> list = new ArrayList<>();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", ordinal = 1))
    public void renderScreen(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        var a = MiscKt.measureTime(() -> {
            ScreenManager.hasScreen(screen -> {
                if (screen.getVisible()) {
                    screen.getRender().invoke(context.nextFrame(client.getLastFrameDuration()));
                }
            });
        });
        list.add(a.getSecond());
        if (list.size() >= 100) {
            TestScreenKt.setFRAME_TIME(list.stream().mapToLong((it) -> it).average().getAsDouble());
            list.clear();
        }
    }


}
