package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.event.KeyPressEvent
import moe.forpleuvoir.ibukigourd.input.Keyboard
import moe.forpleuvoir.ibukigourd.util.soundManager

abstract class IGPressableWidgetImpl : IGClickableWidgetImpl(), IGPressableWidget {

    override var pressed: Boolean = false
        protected set

    override fun onClick(mouseX: Float, mouseY: Float) {
        pressed = true
        this.onPress()
    }

    override fun onRelease(mouseX: Float, mouseY: Float) {
        if (pressed) {
            pressed = false
            onRelease()
        }
    }

    override fun onKeyPress(event: KeyPressEvent) {
        if (!this.visible) return
        event.tryUse { Keyboard.isToggle(event.keyCode) && isFocused }
            .onSuccess {
                this.playClickSound(soundManager)
                this.onPress()
            }
    }

}