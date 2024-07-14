package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.event.KeyPressEvent
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.util.soundManager

abstract class IGPressableWidget : IGClickableWidget() {

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

    override fun onKeyPress(event: KeyPressEvent) {
        if (!this.visible) return
        event.tryUse { Keyboard.isToggle(event.keyCode) }
            .onSuccess {
                this.playClickSound(soundManager)
                this.onPress()
            }
    }


    protected val pressedOrDisabled: Boolean get() = this.active || pressed

    protected fun <T> status(disabled: T, idle: T, hovered: T, pressed: T): T {
        return if (active) {
            if (this.pressed) pressed
            else if (this.wasMouseOver || this.isFocused) hovered
            else idle
        } else disabled
    }

    protected inline fun <R> status(disabled: () -> R, idle: () -> R, hovered: () -> R, pressed: () -> R): R {
        return if (active) {
            if (this.pressed) pressed()
            else if (this.wasMouseOver || this.isFocused) hovered()
            else idle()
        } else disabled()
    }

}