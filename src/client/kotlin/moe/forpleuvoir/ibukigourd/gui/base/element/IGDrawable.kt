package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.input.mouseX
import moe.forpleuvoir.ibukigourd.input.mouseY
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable

interface IGDrawable : Drawable {

    //------------ Vanilla Drawable ------------\\

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        render.invoke(context.toIGDrawContext(), context.client.mouseX, context.client.mouseY, delta)
    }

    //------------ IGDrawable ------------\\

    var layer: GuiLayer

    /**
     * 可见
     */
    var visible: Boolean

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