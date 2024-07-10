package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.Padding
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.util.soundManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.input.KeyCodes
import net.minecraft.text.Text

abstract class IGPressableWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    message: Text,
    padding: Padding = Padding(0)
) : IGClickableWidget(x, y, width, height, message, padding) {

    open var pressed: Boolean = false
        protected set

    abstract fun onPress()

    open fun onRelease() {}

    override fun onClick(mouseX: Double, mouseY: Double) {
        pressed = true
        this.onPress()
    }

    override fun onRelease(mouseX: Double, mouseY: Double) {
        pressed = false
        onRelease()
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!this.active || !this.visible) {
            return false
        } else if (Keyboard.isToggle(keyCode)) {
            this.playClickSound(soundManager)
            this.onPress()
            return true
        } else {
            return false
        }
    }

    protected val pressOrDisabled: Boolean get() = this.active || pressed

    protected fun <T> status(disabled: T, idle: T, hovered: T, pressed: T): T {
        return if (active) {
            if (this.pressed) pressed
            else if (this.hovered || this.isFocused) hovered
            else idle
        } else disabled
    }

    protected inline fun <R> status(disabled: () -> R, idle: () -> R, hovered: () -> R, pressed: () -> R): R {
        return if (active) {
            if (this.pressed) pressed()
            else if (this.hovered || this.isFocused) hovered()
            else idle()
        } else disabled()
    }

}