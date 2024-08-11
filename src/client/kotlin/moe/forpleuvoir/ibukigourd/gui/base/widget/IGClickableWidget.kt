package moe.forpleuvoir.ibukigourd.gui.base.widget

import moe.forpleuvoir.ibukigourd.input.Mouse
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundManager

interface IGClickableWidget : IGWidget {

    fun onClick(mouseX: Float, mouseY: Float) {}

    fun onRelease(mouseX: Float, mouseY: Float) {}


    val clickSound: PositionedSoundInstance?

    val releaseSound: PositionedSoundInstance?

    fun playClickSound(soundManager: SoundManager) {
        clickSound?.let { soundManager.play(it) }
    }

    fun playReleaseSound(soundManager: SoundManager) {
        releaseSound?.let { soundManager.play(it) }
    }

    fun isValidClickButton(button: Mouse): Boolean = button == Mouse.LEFT

    fun allowClicked(mouseX: Float, mouseY: Float): Boolean {
        return transform.isMouseOvered(mouseX, mouseY)
    }

}