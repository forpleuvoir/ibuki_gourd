package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseEnterEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseLeaveEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MousePressEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseReleaseEvent
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.mouseHover
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.input.MouseCursor
import moe.forpleuvoir.ibukigourd.util.Tick
import moe.forpleuvoir.ibukigourd.util.soundManager
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents

abstract class PressableElement(
    modifier: Modifier = Modifier
) : AbstractElement(modifier) {

    /**
     * 是否被按下
     */
    open var pressed: Boolean = false
        protected set

    open var pressTickCounter: Tick = 0
        protected set

    open var longPressTime: Tick = 20

    protected open var longPress: () -> Unit = {}

    protected open val onPress: () -> Unit = {}

    protected open val onRelease: () -> Unit = {}

    var playClickSound: Boolean = true

    override fun tick() {
        if (pressed) {
            pressTickCounter++
            if (longPressTime == pressTickCounter) {
                longPress()
            }
        } else if (pressTickCounter != 0L) {
            pressTickCounter = 0
        }
        super.tick()
    }

    override fun onMouseEnter(event: MouseEnterEvent) {
        MouseCursor.current = MouseCursor.Cursor.POINTING_HAND_CURSOR
    }

    override fun onMouseLeave(event: MouseLeaveEvent) {
        MouseCursor.current = MouseCursor.Cursor.ARROW_CURSOR
    }

    override fun onMouseClick(event: MousePressEvent) {
        super.onMouseClick(event)

        event.tryUse {
            event.button == Mouse.LEFT && mouseHover()
        }.onSuccess {
            if (playClickSound) soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            pressed = true
            onPress()
        }
    }

    override fun onMouseRelease(event: MouseReleaseEvent) {
        super.onMouseRelease(event)
        event.canUse {
            if (event.button == Mouse.LEFT && pressed) {
                pressed = false
                onRelease()
                event.use()
            }
        }
    }


    protected fun <T> status(disabled: T, idle: T, hovered: T, pressed: T): T {
        return if (active) {
            if (this.pressed) pressed
            else if (mouseHover()) hovered
            else idle
        } else disabled
    }

    protected fun <R> status(disabled: () -> R, idle: () -> R, hovered: () -> R, pressed: () -> R): R {
        return if (active) {
            if (this.pressed) pressed()
            else if (mouseHover()) hovered()
            else idle()
        } else disabled()
    }

}