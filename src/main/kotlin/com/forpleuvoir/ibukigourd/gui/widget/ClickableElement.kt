package com.forpleuvoir.ibukigourd.gui.widget

import com.forpleuvoir.ibukigourd.gui.base.AbstractElement
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

	override fun onMouseClick(mouseX: Number, mouseY: Number, button: Mouse): NextAction {
		if (!active) return NextAction.Continue
		if (button == Mouse.LEFT) {
			soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
			pressed = true
			return onClick()
		}
		return super.onMouseClick(mouseX, mouseY, button)
	}

	override fun onMouseRelease(mouseX: Number, mouseY: Number, button: Mouse): NextAction {
		if (!active) return NextAction.Continue
		if (button == Mouse.LEFT) {
			pressed = true
			return onRelease()
		}
		return super.onMouseRelease(mouseX, mouseY, button)
	}


	protected fun <T> status(idle: T, hovered: T, pressed: T): T {
		return if (active)
			if (transform.mouseHover())
				if (this.pressed)
					pressed
				else hovered
			else idle
		else pressed
	}
}