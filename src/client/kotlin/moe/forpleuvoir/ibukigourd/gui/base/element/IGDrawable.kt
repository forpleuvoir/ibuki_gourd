package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.GuiContext
import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.input.mouseX
import moe.forpleuvoir.ibukigourd.input.mouseY
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable

interface IGDrawable : Drawable, GuiContext {

    //------------ Vanilla Drawable ------------\\

    /**
     * 使用原版渲染方式执行绘制操作。
     *
     * 仅用于区分原版与模组的渲染函数
     *
     * @param context 绘制上下文，包含当前绘制所需的环境信息。
     * @param mouseX 鼠标的 X 坐标，以 Number 类型传入，内部会转换为 Int 类型。
     * @param mouseY 鼠标的 Y 坐标，以 Number 类型传入，内部会转换为 Int 类型。
     * @param delta 渲染的增量时间，用于平滑动画或过渡效果。
     */
    fun vanillaRender(context: DrawContext, mouseX: Number, mouseY: Number, delta: Float) =
        render(context, mouseX.toInt(), mouseY.toInt(), delta)

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        render.invoke(context.toIGDrawContext(), context.client.mouseX, context.client.mouseY, delta)
    }

    //------------ IGDrawable ------------\\

    override var layer: GuiLayer

    override fun clearLayer()

    /**
     * 可见
     */
    var visible: Boolean

    fun clearVisible()

    /**
     * 渲染优先级,越低越先渲染.会导致被更高[renderPriority]的[IGDrawable]覆盖
     */
    var renderPriority: Int

    var renderBackground: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit

    fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float)

    var renderOverlay: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit

    fun onRenderOverlay(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float)

    var render: (context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) -> Unit

    fun onRender(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float)


    //------------ Extension ------------\\

    fun IGDrawContext.postRender(render: IGDrawContext.() -> Unit) {
        this.postRender(this@IGDrawable.renderPriority, render)
    }

    fun IGDrawContext.canRender(): Boolean {
        return this.canRender(this@IGDrawable)
    }

    fun IGDrawContext.tryRender(block: IGDrawContext.() -> Unit) {
        this.tryRender(this@IGDrawable, block)
    }

}