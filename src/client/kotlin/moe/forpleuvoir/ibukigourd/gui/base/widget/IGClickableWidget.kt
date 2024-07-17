package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.event.MousePressEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseReleaseEvent
import moe.forpleuvoir.ibukigourd.input.Mouse
import moe.forpleuvoir.ibukigourd.util.soundManager
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.sound.SoundEvents

abstract class IGClickableWidget : WidgetContainerImpl() {

    open fun onClick(mouseX: Double, mouseY: Double) {}

    open fun onRelease(mouseX: Double, mouseY: Double) {}


    var clickSound: PositionedSoundInstance? = PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f)

    var releaseSound: PositionedSoundInstance? = null

    protected fun playClickSound(soundManager: SoundManager) {
        clickSound?.let { soundManager.play(it) }
    }

    protected fun playReleaseSound(soundManager: SoundManager) {
        releaseSound?.let { soundManager.play(it) }
    }

    protected open fun isValidClickButton(button: Mouse): Boolean = button == Mouse.LEFT

    protected open fun clicked(mouseX: Double, mouseY: Double): Boolean {
        return transform.isMouseOvered(mouseX, mouseY)
    }

    override fun onMousePress(event: MousePressEvent) {
        if (!visible) return
        super.onMousePress(event)
        if (isValidClickButton(event.button) && this.clicked(event.x, event.y)) {
            event.tryUse().onSuccess {
                this.playClickSound(soundManager)
                this.onClick(event.x, event.y)
            }
        }
    }

    override fun onMouseRelease(event: MouseReleaseEvent) {
        if (this.isValidClickButton(event.button)) {
            this.onRelease(event.x, event.y)
            this.playReleaseSound(soundManager)
        }
    }

}