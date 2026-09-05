package moe.forpleuvoir.ibukigourd.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import moe.forpleuvoir.ibukigourd.render.extension.state.ItemRenderStateExtensionKt;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.pip.OversizedItemRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PictureInPictureRenderer.class)
public class PictureInPictureRendererMixin {

    @ModifyArg(
            method = "blitTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/state/gui/BlitRenderState;<init>(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2fc;IIIIFFFFILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V"
            ),
            index = 11
    )
    private int modifyPiPBlitColor(int color, @Local(name = "renderState") PictureInPictureRenderState renderState) {
        if (color == 0xFFFFFFFF && renderState instanceof OversizedItemRenderState) {
            return ItemRenderStateExtensionKt.getColor((OversizedItemRenderState) renderState);
        }
        return color;
    }

}
