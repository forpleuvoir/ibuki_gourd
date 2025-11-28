package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.GuiContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics.Companion.toIGGUIGraphics
import moe.forpleuvoir.ibukigourd.input.mouseX
import moe.forpleuvoir.ibukigourd.input.mouseY
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable

interface GuiRenderable : Renderable, GuiContext {

    //------------ Vanilla Drawable ------------\\

    /**
     * 使用原版渲染方式执行绘制操作。
     *
     * 仅用于区分原版与模组的渲染函数
     *
     * @param guiGraphics 绘制上下文，包含当前绘制所需的环境信息。
     * @param mouseX 鼠标的 X 坐标，以 Number 类型传入，内部会转换为 Int 类型。
     * @param mouseY 鼠标的 Y 坐标，以 Number 类型传入，内部会转换为 Int 类型。
     * @param delta 1 delta = 1 Tick 渲染的增量时间，用于平滑动画或过渡效果。
     */
    fun vanillaRender(guiGraphics: GuiGraphics, mouseX: Number, mouseY: Number, delta: Float) =
        render(guiGraphics, mouseX.toInt(), mouseY.toInt(), delta)

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        render.invoke(guiGraphics.toIGGUIGraphics(), guiGraphics.minecraft.mouseX, guiGraphics.minecraft.mouseY, delta)
    }

    //------------ IGDrawable ------------\\

    /**
     * 可见
     */
    var visible: Boolean

    fun clearVisible()

    /**
     * 渲染优先级,越低越先渲染.会导致被更高[renderPriority]的[GuiRenderable]覆盖
     */
    var renderPriority: Int

    var renderBackground: (guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) -> Unit

    fun onRenderBackground(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float)

    var renderOverlay: (guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) -> Unit

    fun onRenderOverlay(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float)

    var render: (guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) -> Unit

    fun onRender(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float)

    //------------ Extension ------------\\

    fun IGGuiGraphics.postRender(render: IGGuiGraphics.() -> Unit) {
        this.postRender(this@GuiRenderable.renderPriority, render)
    }

}
