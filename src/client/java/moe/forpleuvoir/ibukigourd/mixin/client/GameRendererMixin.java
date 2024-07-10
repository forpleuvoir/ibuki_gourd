package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.gui.render.ScissorStack;
import moe.forpleuvoir.ibukigourd.gui.render.context.RenderContext;
import moe.forpleuvoir.ibukigourd.gui.screen.ScreenManager;
import moe.forpleuvoir.ibukigourd.util.MiscKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    MinecraftClient client;

    @Shadow
    @Final
    private BufferBuilderStorage buffers;

    @Unique
    private static RenderContext context;

//    @Unique
//    private static final Deque<Long> list = new ArrayDeque<>();
//    @Unique
//    private static Long tickDeltaCounter = 0L;
//    @Unique
//    private static int temp = 0;
//    @Shadow
//    @Final
//    MinecraftClient client;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", ordinal = 1))
    public void ibukigourd$renderScreen(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (context == null) {
            context = new RenderContext(this.client, this.client.textRenderer, this.buffers.getEntityVertexConsumers(), new MatrixStack(), new ScissorStack());
        }
        if (MiscKt.isDevEnv()) {
            ScreenManager.hasScreen(screen -> {
                var delta = MiscKt.measureTime(() -> {
                    if (screen.getVisible()) {
                        screen.getRender().invoke(context.nextFrame(tickCounter));
                        context.render();
                    }
                }).getSecond();

//                tickDeltaCounter += delta;
//
//                if (list.size() > 500) {
//                    list.pop();
//                }
//                list.addLast(delta);
//
//                if (tickDeltaCounter / 250_000_000 > temp) {
//                    temp++;
//                    var frt = list.stream().mapToLong((it) -> it).average().orElse(0.0);
//                    TestScreenKt.setFRT(frt);
//                    TestScreenKt.setFPS((int) (1000_000_000 / frt));
//                }
//                if (tickDeltaCounter >= 1000_000_000) {
//                    tickDeltaCounter = 0L;
//                    temp = 1;
//                }
            });
        } else {
            ScreenManager.hasScreen(screen -> {
                if (screen.getVisible()) {
                    screen.getRender().invoke(context.nextFrame(tickCounter));
                    context.render();
                }
            });
        }
    }


}
