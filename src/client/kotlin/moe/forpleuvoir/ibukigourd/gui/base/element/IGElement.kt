package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.GuiContext
import moe.forpleuvoir.ibukigourd.gui.base.ModifiableUserInteractionHandler
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.navigation.GuiNavigation
import net.minecraft.client.gui.navigation.GuiNavigationPath

interface IGElement : Element, GuiContext, ModifiableUserInteractionHandler {

    val screen: () -> IGScreen?

    var parent: () -> IGElement?

    var active: Boolean

    fun clearActive()

    //------------ Vanilla Element ------------\\

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (active) mouseMove(MouseMoveEvent(mouseX.toFloat(), mouseY.toFloat()))
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (active) mousePress(MousePressEvent(mouseX.toFloat(), mouseY.toFloat(), Mouse.fromCode(button)))
        return false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (active) mouseRelease(MouseReleaseEvent(mouseX.toFloat(), mouseY.toFloat(), Mouse.fromCode(button)))
        return false
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (active) mouseDragging(MouseDragEvent(mouseX.toFloat(), mouseY.toFloat(), Mouse.fromCode(button), deltaX.toFloat(), deltaY.toFloat()))
        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (active) mouseScrolling(MouseScrollEvent(mouseX.toFloat(), mouseY.toFloat(), verticalAmount.toFloat(), horizontalAmount.toFloat()))
        return false
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (active) keyPress(KeyPressEvent(Keyboard.fromCode(keyCode), scanCode, modifiers))
        return false
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (active) keyRelease(KeyReleaseEvent(Keyboard.fromCode(keyCode), scanCode, modifiers))
        return false
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        if (active) charTyped.invoke(CharTypedEvent(chr, modifiers))
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

    fun Modifier.foldInApply() {
        this.foldIn(Unit) { _, m -> m.tryApplyModify(this@IGElement) }
    }

    fun Modifier.foldOutApply() {
        this.foldOut(Unit) { m, _ -> m.tryApplyModify(this@IGElement) }
    }


}

