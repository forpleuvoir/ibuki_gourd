package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.event.GUIEvent.Companion.layer
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.navigation.GuiNavigation
import net.minecraft.client.gui.navigation.GuiNavigationPath

interface IGElement : Element, ModifiableUserInteractionHandler {

    val screen: () -> IGScreen?

    var parent: () -> IGElement?

    var layer: GuiLayer

    var active: Boolean

    //------------ Vanilla Element ------------\\

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (active) mouseMove(MouseMoveEvent(mouseX, mouseY).layer(layer))
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (active) mouseClick(MousePressEvent(mouseX, mouseY, Mouse.fromCode(button)).layer(layer))
        return false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (active) mouseRelease(MouseReleaseEvent(mouseX, mouseY, Mouse.fromCode(button)).layer(layer))
        return false
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (active) mouseDragging(MouseDragEvent(mouseX, mouseY, Mouse.fromCode(button), deltaX, deltaY).layer(layer))
        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (active) mouseScrolling(MouseScrollEvent(mouseX, mouseY, horizontalAmount, verticalAmount).layer(layer))
        return false
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (active) keyPress(KeyPressEvent(Keyboard.fromCode(keyCode), scanCode, modifiers).layer(layer))
        return false
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (active) keyRelease(KeyReleaseEvent(Keyboard.fromCode(keyCode), scanCode, modifiers).layer(layer))
        return false
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        if (active) charTyped.invoke(CharTypedEvent(chr, modifiers).layer(layer))
        return false
    }

    override fun getNavigationPath(navigation: GuiNavigation?): GuiNavigationPath? = null


    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean

    override fun setFocused(focused: Boolean)

    override fun isFocused(): Boolean

    override fun getFocusedPath(): GuiNavigationPath? =
        if (this.isFocused) GuiNavigationPath.of(this) else null

    override fun getNavigationFocus(): ScreenRect =
        ScreenRect.empty()


    //------------ Extensions ------------\\

    /**
     * Executes the use function on the current GUIEvent instance with the given Element.
     * @receiver GUIEvent The current GUIEvent instance.
     */
    fun GUIEvent.use() =
        this.use(this@IGElement)

    /**
     * Tries to use the current GUIEvent instance with the given block of code.
     * If the GUIEvent can be used and the block returns true, the 'use' function is executed on the element, and true is returned.
     * If the GUIEvent cannot be used or the block returns false, false is returned.
     * ```kotlin
     * event.tryUse{
     *     //if used return true
     *     true
     * }.onSuccess{
     *     //do something
     * }
     * ```
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
    fun GUIEvent.canUse(): Boolean =
        this.canUse(this@IGElement)


    fun GUIEvent.canUse(block: () -> Unit) =
        this.canUse(this@IGElement, block)


    fun GUIEvent.cantUse(): Boolean =
        this.cantUse(this@IGElement)


    fun GUIEvent.cantUse(block: () -> Unit) =
        this.canUse(this@IGElement, block)


}