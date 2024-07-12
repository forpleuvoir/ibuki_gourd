package moe.forpleuvoir.ibukigourd.gui.base.screen

import moe.forpleuvoir.ibukigourd.gui.base.Transform
import moe.forpleuvoir.ibukigourd.gui.base.element.IGDrawable
import moe.forpleuvoir.ibukigourd.gui.base.element.IGElement
import moe.forpleuvoir.ibukigourd.gui.base.extensions.drawcontent.renderGradientBox
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext.Companion.toIGDrawContext
import moe.forpleuvoir.ibukigourd.gui.base.render.arrange.Orientation
import moe.forpleuvoir.ibukigourd.gui.base.widget.IGWidget
import moe.forpleuvoir.ibukigourd.input.mousePosition
import moe.forpleuvoir.ibukigourd.render.math.Vector2f
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.nebula.common.color.Color
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen

abstract class IGScreenImpl : Screen(Literal("ibuki gourd screen")), IGScreen {

    //------------ IGScreen ------------\\

    override val transform: Transform = Transform(Vector2f(0f, 0f), this.width.toFloat(), this.height.toFloat(), true).apply {
        subscribeSizeChange { _, (width, height) ->
            this@IGScreenImpl.width = width.toInt()
            this@IGScreenImpl.height = height.toInt()
        }
    }


    //------------ Tickable ------------\\

    override var tick: () -> Unit = ::tick

    override fun tick() {
        super<IGScreen>.tick()
    }

    //------------ Container ------------\\

    //------------ Container.Element ------------\\

    protected val elementChildren = mutableListOf<IGElement>()

    @Deprecated("should use elementChildren() instead", ReplaceWith("elementChildren"))
    override fun children(): MutableList<out Element> = elementChildren

    override fun elementChildren(): List<IGElement> = elementChildren

    override fun <T : IGElement> addElementChild(child: T): T = child.also {
        it.parent = { this }
        elementChildren.add(it)
    }

    protected val drawableChildren = mutableListOf<IGDrawable>()

    override fun drawableChildren(): List<IGDrawable> = drawableChildren

    @Deprecated("should use addDrawableChild(child) instead", ReplaceWith("addDrawableChild(drawable)"))
    override fun <T : Drawable?> addDrawable(drawable: T): T = drawable

    override fun <T : IGDrawable> addDrawableChild(child: T): T = child.also {
        drawableChildren.add(it)
    }

    protected val widgetChildren = mutableListOf<IGWidget>()

    override fun widgetChildren(): List<IGWidget> = widgetChildren

    @Deprecated("should use addWidgetChild(child) instead", ReplaceWith("addWidgetChild"))
    override fun <T> addSelectableChild(child: T): T where T : Element, T : Selectable {
        return super.addSelectableChild(child)
    }

    override fun <W : IGWidget> addWidgetChild(child: W): W = child.also {
        add
        widgetChildren.add(it)
    }


    //------------ Vanilla Screen Override ------------\\

    var pauseGame: Boolean = false

    override fun shouldPause(): Boolean {
        return pauseGame
    }

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        super.resize(client, width, height)
        transform.width = width.toFloat()
        transform.height = height.toFloat()
    }


    //------------ Drawable ------------\\

    override var visible: Boolean = true

    override var renderPriority: Int = 0

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val ctx = context.toIGDrawContext()
        val (_mouseX, _mouseY) = context.client.mousePosition
        ctx.tryRender {
            renderBackground(this, _mouseX, _mouseY, delta)
            render.invoke(this, _mouseX, _mouseY, delta)
        }

        for (drawableChild in drawableChildren().sortedBy { it.renderPriority }) {
            ctx.tryRender(drawableChild) {
                drawableChild.render.invoke(this, _mouseX, _mouseY, delta)
            }
        }

        ctx.tryRender { renderOverlay(this, _mouseX, _mouseY, delta) }
    }

    override fun onRenderBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) {
        renderVanillaBackground(context, mouseX, mouseY, delta)
    }


    //------------ Vanilla Drawable Override ------------\\

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun renderVanillaBackground(context: IGDrawContext, mouseX: Float, mouseY: Float, delta: Float) =
        renderBackground(context, mouseX.toInt(), mouseY.toInt(), delta)

    override fun renderDarkening(context: DrawContext) {
        renderDarkening(context, transform.worldX.toInt(), transform.worldY.toInt(), width, height)
    }

    override fun renderInGameBackground(context: DrawContext) {
        context.renderGradientBox(transform.asWorldBox, Color(0xC0101010), Color(0xD0101010), Orientation.Vertical)
    }


    //------------ Vanilla Element Override ------------\\


}