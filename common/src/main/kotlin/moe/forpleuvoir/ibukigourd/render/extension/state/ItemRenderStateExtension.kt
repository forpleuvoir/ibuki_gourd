@file:Suppress("CAST_NEVER_SUCCEEDS")

package moe.forpleuvoir.ibukigourd.render.extension.state

import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.renderer.state.gui.GuiItemRenderState
import net.minecraft.client.renderer.state.gui.pip.OversizedItemRenderState

interface ItemRenderStateExtension {

    fun setColor(color: Int)

    fun getColor(): Int

}

fun GuiItemRenderState.setColor(color: Color) {
    (this as ItemRenderStateExtension).setColor(color.argb)
}


fun GuiItemRenderState.getColor(): Int {
    return (this as ItemRenderStateExtension).getColor()
}

fun OversizedItemRenderState.setColor(color: Color) {
    (this as ItemRenderStateExtension).setColor(color.argb)
}

fun OversizedItemRenderState.getColor(): Int {
    return (this as ItemRenderStateExtension).getColor()
}