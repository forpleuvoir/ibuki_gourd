@file:Suppress("CAST_NEVER_SUCCEEDS")

package moe.forpleuvoir.ibukigourd.render.extension.state

import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.renderer.state.gui.GuiItemRenderState
import net.minecraft.client.renderer.state.gui.pip.OversizedItemRenderState

interface ItemRenderStateExtension {

    fun setColor(color: Int)

    fun getColor(): Int

}

/**
 * 将颜色预乘 alpha：RGB 通道乘以 alpha。
 *
 * 渲染物品图集走的是 [net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA] 中的预乘 alpha 管线，
 * 顶点色作为采样结果的乘数，需要 RGB 与 alpha 同步缩放，
 * 否则单独降低 alpha 会导致合成结果发白（覆盖率下降但颜色贡献未降）。
 *
 * alpha == 1 时为恒等操作，因此纯白染色不会受影响，
 * 只在 alpha < 1 的透明度调节时生效。
 */
val Color.premultiplied: Color
    get() {
        val a = alphaF
        return Color.fromARGB(redF * a, greenF * a, blueF * a, a)
    }

fun GuiItemRenderState.setColor(color: Color) {
    (this as ItemRenderStateExtension).setColor(color.premultiplied.argb)
}


fun GuiItemRenderState.getColor(): Int {
    return (this as ItemRenderStateExtension).getColor()
}

fun OversizedItemRenderState.setColor(color: Color) {
    (this as ItemRenderStateExtension).setColor(color.premultiplied.argb)
}

fun OversizedItemRenderState.getColor(): Int {
    return (this as ItemRenderStateExtension).getColor()
}