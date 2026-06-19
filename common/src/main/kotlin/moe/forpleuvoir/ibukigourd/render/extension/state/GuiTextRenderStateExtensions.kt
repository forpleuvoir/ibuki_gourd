@file:Suppress("CAST_NEVER_SUCCEEDS")

package moe.forpleuvoir.ibukigourd.render.extension.state

import net.minecraft.client.renderer.state.gui.GuiTextRenderState

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