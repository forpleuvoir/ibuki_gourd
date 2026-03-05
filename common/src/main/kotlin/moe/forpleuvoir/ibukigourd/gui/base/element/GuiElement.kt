package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.GuiContext
import moe.forpleuvoir.ibukigourd.gui.base.ModifiableGuiHandler
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.screen.IGScreen
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.input.Mouse
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.gui.navigation.ScreenDirection
import net.minecraft.client.gui.navigation.ScreenRectangle

interface GuiElement : GuiEventListener, GuiContext, ModifiableGuiHandler {

    val screen: () -> IGScreen?

    var parent: () -> GuiElement?

    var active: Boolean

    fun clearActive()

    //------------ Vanilla Element ------------\\

    @Deprecated("如果要覆写,请使用onProcess方法", replaceWith = ReplaceWith("onProcess(delta)"))
    override fun process(delta: Float) {
        process.invoke(delta)
    }

    override fun onProcess(delta: Float) = Unit

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (active) mouseMove(MouseMoveEvent(mouseX.toFloat(), mouseY.toFloat()))
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (active) mousePress(MousePressEvent(mouseX.toFloat(), mouseY.toFloat(), Mouse.fromCode(button), false))
        return false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (active) mouseRelease(MouseReleaseEvent(mouseX.toFloat(), mouseY.toFloat(), Mouse.fromCode(button)))
        return false
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (active) mouseDragging(MouseDragEvent(mouseX.toFloat(), mouseY.toFloat(), Mouse.fromCode(button), dragX.toFloat(), dragY.toFloat()))
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

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        if (active) charTyped.invoke(CharTypedEvent(codePoint.code, modifiers))
        return false
    }

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? = null

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean

    override fun setFocused(focused: Boolean)

    override fun isFocused(): Boolean

    override fun getCurrentFocusPath(): ComponentPath? = if (this.isFocused) ComponentPath.leaf(this) else null

    override fun getBorderForArrowNavigation(direction: ScreenDirection): ScreenRectangle = ScreenRectangle.empty()


    //------------ Extensions ------------\\

    fun Modifier.foldInApply() {
        this.foldIn(Unit) { _, m -> m.tryApplyModify(this@GuiElement) }
    }

    fun Modifier.foldOutApply() {
        this.foldOut(Unit) { m, _ -> m.tryApplyModify(this@GuiElement) }
    }


}

