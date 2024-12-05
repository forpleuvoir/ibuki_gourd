package moe.forpleuvoir.ibukigourd.gui.base.element

import moe.forpleuvoir.ibukigourd.gui.base.GuiContext
import moe.forpleuvoir.ibukigourd.gui.base.GuiLayer
import moe.forpleuvoir.ibukigourd.gui.base.ModifiableUserInteractionHandler
import moe.forpleuvoir.ibukigourd.gui.base.event.*
import moe.forpleuvoir.ibukigourd.gui.base.event.GUIEvent.Companion.layer
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

    val customData: MutableMap<String, Any>



    fun hasParentInChain(target: IGElement): Boolean {
        var current: IGElement? = this
        while (current != null && current.parent() != current) {
            if (current == target) {
                return true
            }
            current = current.parent()
        }
        return current == target
    }

    override var layer: GuiLayer

    var active: Boolean

    fun clearActive()

    //------------ Vanilla Element ------------\\

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (active) mouseMove(MouseMoveEvent(mouseX.toFloat(), mouseY.toFloat()).layer(layer))
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (active) mousePress(MousePressEvent(mouseX.toFloat(), mouseY.toFloat(), Mouse.fromCode(button)).layer(layer))
        return false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (active) mouseRelease(MouseReleaseEvent(mouseX.toFloat(), mouseY.toFloat(), Mouse.fromCode(button)).layer(layer))
        return false
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (active) mouseDragging(MouseDragEvent(mouseX.toFloat(), mouseY.toFloat(), Mouse.fromCode(button), deltaX.toFloat(), deltaY.toFloat()).layer(layer))
        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (active) mouseScrolling(MouseScrollEvent(mouseX.toFloat(), mouseY.toFloat(), verticalAmount.toFloat(), horizontalAmount.toFloat()).layer(layer))
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

    fun Modifier.foldInApply() {
        this.foldIn(Unit) { _, m -> m.tryApplyModify(this@IGElement) }
    }

    fun Modifier.foldOutApply() {
        this.foldOut(Unit) { m, _ -> m.tryApplyModify(this@IGElement) }
    }

    /**
     * Executes the use function on the current GUIEvent instance with the given Element.
     * @receiver GUIEvent The current GUIEvent instance.
     */
    fun GUIEvent.use() =
        this.use(this@IGElement)

    /**
     * 尝试使用当前的 GUIEvent 实例与给定的代码块。
     * 如果 GUIEvent 可以使用并且代码块返回 true，则在该元素上执行 'use' 函数，并返回 true。
     * 如果 GUIEvent 不能使用或代码块返回 false，则返回 false。
     * ```kotlin
     * event.tryUse {
     *     // 如果使用成功，返回 true
     *     true
     * }.onSuccess {
     *     // 执行一些操作
     * }
     * ```
     * @receiver GUIEvent 当前的 GUIEvent 实例。
     * @param block 要执行的代码块。
     * @return Result<Boolean> 成功（true）如果事件已使用，失败（false）否则。
     */
    fun GUIEvent.tryUse(condition: () -> Boolean): Result<Unit> {
        if (canUse(this@IGElement) && condition()) {
            this.use(this@IGElement)
            return Result.success(Unit)
        }
        return Result.failure(Exception("Event cannot be used."))
    }

    fun GUIEvent.tryUse(condition: Boolean = true): Result<Unit> {
        if (canUse(this@IGElement) && condition) {
            this.use(this@IGElement)
            return Result.success(Unit)
        }
        return Result.failure(Exception("Event cannot be used."))
    }

    /**
     * 检查 GUIEvent 是否可以使用
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

