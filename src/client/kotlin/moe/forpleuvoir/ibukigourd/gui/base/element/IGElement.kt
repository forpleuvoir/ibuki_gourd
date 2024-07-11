package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.render.IGDrawContext
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.navigation.GuiNavigation
import net.minecraft.client.gui.navigation.GuiNavigationPath

interface IGElement : Element, ModifiableUserInteractionHandler {

    var layer: GuiLayer

    //------------ Vanilla Element ------------\\

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        mouseMove(MouseMoveEvent(mouseX, mouseY))
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        mouseClick(MousePressEvent(mouseX, mouseY, Mouse.fromCode(button)))
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        mouseRelease(MouseReleaseEvent(mouseX, mouseY, Mouse.fromCode(button)))
        return true
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        mouseDragging(MouseDragEvent(mouseX, mouseY, Mouse.fromCode(button), deltaX, deltaY))
        return true
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        mouseScrolling(MouseScrollEvent(mouseX, mouseY, horizontalAmount, verticalAmount))
        return true
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        keyPress(KeyPressEvent(Keyboard.fromCode(keyCode), scanCode, modifiers))
        return true
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        keyRelease(KeyReleaseEvent(Keyboard.fromCode(keyCode), scanCode, modifiers))
        return true
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        charTyped.invoke(CharTypedEvent(chr, modifiers))
        return true
    }

    override fun getNavigationPath(navigation: GuiNavigation?): GuiNavigationPath? {
        return null
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean

    override fun setFocused(focused: Boolean)

    override fun isFocused(): Boolean

    override fun getFocusedPath(): GuiNavigationPath? {
        return if (this.isFocused) GuiNavigationPath.of(this) else null
    }

    override fun getNavigationFocus(): ScreenRect {
        return ScreenRect.empty()
    }


    //------------ Extensions ------------\\

    /**
     * Executes the use function on the current GUIEvent instance with the given Element.
     * @receiver GUIEvent The current GUIEvent instance.
     * @param element The Element to use.
     */
    fun GUIEvent.use() {
        this.use(this@IGElement)
    }

    /**
     * Tries to use the current GUIEvent instance with the given block of code.
     * If the GUIEvent can be used and the block returns true, the 'use' function is executed on the element, and true is returned.
     * If the GUIEvent cannot be used or the block returns false, false is returned.
     * @receiver GUIEvent The current GUIEvent instance.
     * @param block The block of code to be executed.
     * @return Result<Boolean> Success(true) if the event was used, Failure(false) otherwise.
     */
    fun GUIEvent.tryUse(block: () -> Boolean = { true }): Result<Boolean> {
        if (canUse(this@IGElement)) {
            if (block()) {
                this.use(this@IGElement)
                return Result.success(true)
            }
            return Result.failure(Exception("Block returned false."))
        }
        return Result.failure(Exception("Event cannot be used."))
    }

    /**
     * Determines if the GUIEvent instance can be used with the given Element.
     *
     * @return true if the GUIEvent can be used with the Element, false otherwise.
     */
    fun GUIEvent.canUse(): Boolean {
        return this.canUse(this@IGElement)
    }

    fun GUIEvent.canUse(block: () -> Unit) {
        this.canUse(this@IGElement, block)
    }

    fun GUIEvent.cantUse(): Boolean {
        return this.cantUse(this@IGElement)
    }

    fun GUIEvent.cantUse(block: () -> Unit) {
        this.canUse(this@IGElement, block)
    }

    fun IGDrawContext.canRender(): Boolean {
        return this.canRender(this@IGElement)
    }

    fun IGDrawContext.tryRender(block: IGDrawContext.() -> Unit) {
        this.tryRender(this@IGElement, block)
    }
}