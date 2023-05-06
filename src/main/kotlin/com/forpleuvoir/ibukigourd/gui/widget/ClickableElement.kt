package com.forpleuvoir.ibukigourd.gui.widget

import com.forpleuvoir.ibukigourd.gui.base.element.AbstractElement
import com.forpleuvoir.ibukigourd.gui.base.mouseHover
import com.forpleuvoir.ibukigourd.input.Mouse
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.ibukigourd.util.soundManager
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents

abstract class ClickableElement : AbstractElement() {

	/**
	 * 是否被按下
	 */
	open var pressed: Boolean = false

	open var onClick: () -> NextAction = { NextAction.Continue }

	open var onRelease: () -> NextAction = { NextAction.Continue }

	override fun onMouseClick(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
		if (!active) return NextAction.Continue
		if (super.onMouseClick(mouseX, mouseY, button) == NextAction.Cancel) return NextAction.Cancel
		if (button == Mouse.LEFT && transform.mouseHover()) {
			soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
			pressed = true
			return onClick()
		}
		return NextAction.Continue
	}

	override fun onMouseRelease(mouseX: Float, mouseY: Float, button: Mouse): NextAction {
		if (!active) return NextAction.Continue
		if (super.onMouseRelease(mouseX, mouseY, button) == NextAction.Cancel) return NextAction.Cancel
		if (button == Mouse.LEFT) {
			pressed = false
			return onRelease()
		}
		return NextAction.Continue
	}


	protected fun <T> status(disabled: T, idle: T, hovered: T, pressed: T): T {
		return if (active) {
			if (this.pressed) pressed
			else if (transform.mouseHover()) hovered
			else idle
		} else disabled
	}

}