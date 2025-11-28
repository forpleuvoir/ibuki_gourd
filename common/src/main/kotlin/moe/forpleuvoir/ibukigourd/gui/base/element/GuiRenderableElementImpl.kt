package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics
import moe.forpleuvoir.ibukigourd.gui.base.render.IGGuiGraphics.Companion.toIGGUIGraphics
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.input.mousePosition
import net.minecraft.client.gui.GuiGraphics

abstract class GuiRenderableElementImpl : GuiRenderableElement {

    //------------ Vanilla Drawable ------------\\

    @Suppress("LocalVariableName")
    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val (_mouseX, _mouseY) = guiGraphics.minecraft.mousePosition
        guiGraphics.toIGGUIGraphics().apply {
            renderBackground.invoke(this, _mouseX, _mouseY, delta)
            render.invoke(this, _mouseX, _mouseY, delta)
            renderOverlay.invoke(this, _mouseX, _mouseY, delta)
        }
    }


    //------------ Tickable ------------\\

    override var tick: () -> Unit = ::onTick

    override fun onTick() {}


    //------------ IGDrawable ------------\\

    private var _visible: Boolean? = null

    override var visible: Boolean
        set(value) {
            _visible = value
        }
        get() {
            val parentVisible = (parent() as? GuiRenderable)?.visible
            return _visible ?: (parentVisible ?: true)
        }

    override fun clearVisible() {
        _visible = null
    }

    override var renderPriority: Int = 0

    override var renderBackground: (guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRenderBackground

    abstract override fun onRenderBackground(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float)

    override var render: (guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRender

    abstract override fun onRender(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float)

    override var renderOverlay: (guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float) -> Unit = ::onRenderOverlay

    abstract override fun onRenderOverlay(guiGraphics: IGGuiGraphics, mouseX: Float, mouseY: Float, delta: Float)


    //------------ IGElement ------------\\

    override val screen: () -> IGScreen?
        get() {
            return if (parent() is IGScreen) {
                { parent() as IGScreen }
            } else {
                { parent()?.screen?.let { it() } }
            }
        }

    override var parent: () -> GuiElement? = { null }

    private var _active: Boolean? = null

    override var active: Boolean
        set(value) {
            _active = value
        }
        get() {
            return _active ?: (parent()?.active ?: true)
        }

    override fun clearActive() {
        _active = null
    }

    override var mouseEnter: (event: MouseEnterEvent) -> Unit = ::onMouseEnter

    override var mouseLeave: (event: MouseLeaveEvent) -> Unit = ::onMouseLeave

    override var mouseMove: (event: MouseMoveEvent) -> Unit = ::onMouseMove

    override var mousePress: (event: MousePressEvent) -> Unit = ::onMousePress

    override var focused: (event: FocusedEvent) -> Unit = ::onFocused

    override var mouseRelease: (event: MouseReleaseEvent) -> Unit = ::onMouseRelease

    override var mouseDragging: (event: MouseDragEvent) -> Unit = ::onMouseDragging

    override var mouseScrolling: (event: MouseScrollEvent) -> Unit = ::onMouseScrolling

    override var keyPress: (event: KeyPressEvent) -> Unit = ::onKeyPress

    override var keyRelease: (event: KeyReleaseEvent) -> Unit = ::onKeyRelease

    override var charTyped: (event: CharTypedEvent) -> Unit = ::onCharTyped

}