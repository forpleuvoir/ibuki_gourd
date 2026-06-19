package moe.forpleuvoir.ibukigourd.render

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.renderer.state.gui.GuiRenderState

interface GuiGraphicsExtractorAccessor {

    fun peekScissorRect(): ScreenRectangle?

    fun guiRenderState(): GuiRenderState

}

fun GuiGraphicsExtractor.peekScissorRect() = (this as GuiGraphicsExtractorAccessor).peekScissorRect()

val GuiGraphicsExtractor.renderState get() = (this as GuiGraphicsExtractorAccessor).guiRenderState()