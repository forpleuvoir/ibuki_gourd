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
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

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

    override fun mouseClicked(event: MouseButtonEvent, isDoubleClick: Boolean): Boolean {
        if (active) mousePress(MousePressEvent(event.x.toFloat(), event.y.toFloat(), Mouse.fromCode(event.button()), isDoubleClick))
        return false
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (active) mouseRelease(MouseReleaseEvent(event.x.toFloat(), event.y.toFloat(), Mouse.fromCode(event.button())))
        return false
    }

    override fun mouseDragged(event: MouseButtonEvent, mouseX: Double, mouseY: Double): Boolean {
        if (active) mouseDragging(MouseDragEvent(event.x.toFloat(), event.y.toFloat(), Mouse.fromCode(event.button()), mouseX.toFloat(), mouseY.toFloat()))
        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (active) mouseScrolling(MouseScrollEvent(mouseX.toFloat(), mouseY.toFloat(), verticalAmount.toFloat(), horizontalAmount.toFloat()))
        return false
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (active) keyPress(KeyPressEvent(Keyboard.fromCode(event.key), event.scancode, event.modifiers))
        return false
    }

    override fun keyReleased(event: KeyEvent): Boolean {
        if (active) keyRelease(KeyReleaseEvent(Keyboard.fromCode(event.key), event.scancode, event.modifiers))
        return false
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        if (active) charTyped.invoke(CharTypedEvent(event.codepoint(), event.modifiers))
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

