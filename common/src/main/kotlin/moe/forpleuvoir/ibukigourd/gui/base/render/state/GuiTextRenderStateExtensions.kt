package moe.forpleuvoir.ibukigourd.gui.base.render.state

import net.minecraft.client.gui.render.state.GuiTextRenderState

interface GuiTextRenderStateExtensions {

    fun setXF(x: Float)

    fun setYF(y: Float)

    fun getXF(): Float

    fun getYF(): Float

}

fun GuiTextRenderState.setXF(x: Float) {
    (this as GuiTextRenderStateExtensions).setXF(x)
}

fun GuiTextRenderState.setYF(y: Float) {
    (this as GuiTextRenderStateExtensions).setYF(y)
}