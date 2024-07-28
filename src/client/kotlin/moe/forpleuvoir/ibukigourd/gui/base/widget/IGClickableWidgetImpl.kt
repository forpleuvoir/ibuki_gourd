package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.gui.base.event.MousePressEvent
import moe.forpleuvoir.ibukigourd.gui.base.event.MouseReleaseEvent
import moe.forpleuvoir.ibukigourd.util.soundManager
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents

abstract class IGClickableWidgetImpl : IGWidgetImpl(), IGClickableWidget {

    override val clickSound: PositionedSoundInstance? = PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f)

    override val releaseSound: PositionedSoundInstance? = null

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