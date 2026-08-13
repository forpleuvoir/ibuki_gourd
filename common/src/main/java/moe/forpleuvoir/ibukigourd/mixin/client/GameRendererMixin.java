package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.ui.util.ComposeScreenHelper;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @ModifyVariable(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlobalSettingsUniform;update(IIDJLnet/minecraft/client/DeltaTracker;ILnet/minecraft/world/phys/Vec3;Z)V"),
            name = "shouldRenderLevel"
    )
    private boolean modifyRenderLevel(boolean shouldRenderLevel) {
        return !ComposeScreenHelper.shouldBlockLevelRender() && shouldRenderLevel;
    }

    @ModifyVariable(
            method = "extract",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;extractOptions()V"),
            name = "readyForLevelRendering"
    )
    private boolean modifyExtractLevel(boolean readyForLevelRendering) {
        return !ComposeScreenHelper.shouldBlockLevelRender() && readyForLevelRendering;
    }

}
