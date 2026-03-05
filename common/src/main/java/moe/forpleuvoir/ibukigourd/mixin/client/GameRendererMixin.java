package moe.forpleuvoir.ibukigourd.mixin.client;

import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics;
import moe.forpleuvoir.ibukigourd.gui.base.tip.TipHandler;
import moe.forpleuvoir.ibukigourd.gui.base.toast.Toast;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.GameRenderer;
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
    private Minecraft minecraft;

    @Shadow
    @Final
    private GuiRenderState guiRenderState;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", ordinal = 0, shift = At.Shift.BEFORE))
    public void render(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
//        , @Local(print = true) GuiGraphics guigraphics, @Local(ordinal = 0, print = true) int mouseX, @Local(ordinal = 1, print = true) int mouseY
//        var ctx = IGGuiGraphics.Companion.toIGGUIGraphics(guigraphics);
        var ctx = IGGuiGraphics.Companion.toIGGUIGraphics(new GuiGraphics(this.minecraft, this.guiRenderState));
        int mouseX = (int) this.minecraft.mouseHandler.getScaledXPos(this.minecraft.getWindow());
        int mouseY = (int) this.minecraft.mouseHandler.getScaledYPos(this.minecraft.getWindow());
        TipHandler.render(ctx, mouseX, mouseY, deltaTracker.getGameTimeDeltaTicks());
        Toast.render(ctx, mouseX, mouseY, deltaTracker.getGameTimeDeltaTicks());
    }

}
