package moe.forpleuvoir.ibukigourd.gui.base.widget

interface PressableWidget : ClickableWidget {

    val pressed: Boolean

    fun onPress() {}

    fun onRelease() {}

    val pressedOrDisabled: Boolean get() = this.active || pressed

    fun <T> status(disabled: T, idle: T, hovered: T, pressed: T): T {
        return if (active) {
            if (this.pressed) pressed
            else if (this.wasMouseOver || this.isFocused) hovered
            else idle
        } else disabled
    }

    fun <R> status(disabled: () -> R, idle: () -> R, hovered: () -> R, pressed: () -> R): R {
        return if (active) {
            if (this.pressed) pressed()
            else if (this.wasMouseOver || this.isFocused) hovered()
            else idle()
        } else disabled()
    }

}