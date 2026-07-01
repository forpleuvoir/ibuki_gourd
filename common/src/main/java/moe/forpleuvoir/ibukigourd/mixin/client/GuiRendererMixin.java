package moe.forpleuvoir.ibukigourd.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import moe.forpleuvoir.ibukigourd.render.extension.state.ItemRenderStateExtensionKt;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @ModifyArgs(
            method = "submitBlitFromItemAtlas",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/state/gui/BlitRenderState;<init>(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2f;IIIIFFFFILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V"
            )
    )
    private void ibukigourd$modifyItemAtlasColor(Args args, @Local(name = "itemState") GuiItemRenderState itemState) {
        var color = ItemRenderStateExtensionKt.getColor(itemState);
        if (((color >> 24) & 0xff) != 255) {
            args.set(0, RenderPipelines.GUI_TEXTURED);
            args.set(11, color);
        }
    }

}
