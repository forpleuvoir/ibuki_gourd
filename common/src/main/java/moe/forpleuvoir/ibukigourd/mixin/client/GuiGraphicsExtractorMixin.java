package moe.forpleuvoir.ibukigourd.mixin.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import moe.forpleuvoir.ibukigourd.render.GuiGraphicsExtractorAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.*;

@Mixin(GuiGraphicsExtractor.class)
@Implements(@Interface(iface = GuiGraphicsExtractorAccessor.class, prefix = "ibukigourd$"))
public class GuiGraphicsExtractorMixin {

    @Shadow
    @Final
    private GuiGraphicsExtractor.ScissorStack scissorStack;

    @Shadow
    @Final
    private GuiRenderState guiRenderState;

    @Nullable
    @Unique
    public ScreenRectangle ibukigourd$peekScissorRect() {
        return scissorStack.peek();
    }

    @NonNull
    @Unique
    public GuiRenderState ibukigourd$guiRenderState() {
        return guiRenderState;
    }

}
