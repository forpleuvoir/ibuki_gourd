package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.GuiContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext

interface IGDrawable : GuiContext {

    //------------ Vanilla Drawable ------------\\

    fun render(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        renderLayers().forEach { layer ->
            layer.renderLayer(context, mouseX, mouseY, delta)
        }
    }

    //------------ IGDrawable ------------\\

    /**
     * 可见
     */
    var visible: Boolean

    fun clearVisible()

    /**
     * 渲染优先级,越低越先渲染.会导致被更高[renderPriority]的[IGDrawable]覆盖
     */
    var renderPriority: Int

    /**
     * 获取需要渲染的图层列表。
     *
     * 每个图层都实现了 [GuiRenderLayer] 接口，用于定义具体的渲染逻辑。
     * 渲染顺序由调用者决定，通常优先级较低的图层会先被渲染，
     * 而优先级较高的图层会覆盖先前渲染的内容。
     *
     * @return 包含已排序的需要渲染的图层的列表，如果没有图层则返回空列表。
     */
    fun renderLayers(): Iterable<GuiRenderLayer>

    fun addRenderLayer(priority: Int, layer: GuiRenderLayer)

    fun removeRenderLayer(priority: Int): Boolean

    //------------ Extension ------------\\

    fun IGDrawContext.postRender(render: IGDrawContext.() -> Unit) {
        this.postRender(this@IGDrawable.renderPriority, render)
    }

}

fun interface GuiRenderLayer {
    fun renderLayer(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float)
}

object RenderPriority {

    /**
     * 在默认渲染之前执行的优先级层级。
     */
    const val PRE_HANDLER: Int = -100

    /**
     * 用于背景元素的渲染层级。
     */
    const val BACKGROUND: Int = -50

    /**
     * 默认渲染层级，适用于大多数基础 UI 元素。
     */
    const val DEFAULT: Int = 0

    /**
     * 用于覆盖在基础元素之上的 UI 层级（例如提示、高亮等）。
     */
    const val OVERLAY: Int = 50

    /**
     * 在默认处理之后立即执行的层级。
     */
    const val POST_HANDLER: Int = 100

}

fun IGDrawable.addPreHandleLayer(layer: GuiRenderLayer) {
    addRenderLayer(RenderPriority.PRE_HANDLER, layer)
}

fun IGDrawable.addBackgroundLayer(layer: GuiRenderLayer) {
    addRenderLayer(RenderPriority.BACKGROUND, layer)
}

fun IGDrawable.addDefaultLayer(layer: GuiRenderLayer) {
    addRenderLayer(RenderPriority.DEFAULT, layer)
}

fun IGDrawable.addOverlayLayer(layer: GuiRenderLayer) {
    addRenderLayer(RenderPriority.OVERLAY, layer)
}

fun IGDrawable.addPostHandleLayer(layer: GuiRenderLayer) {
    addRenderLayer(RenderPriority.POST_HANDLER, layer)
}