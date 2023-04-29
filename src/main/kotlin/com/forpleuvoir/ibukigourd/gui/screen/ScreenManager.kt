package com.forpleuvoir.ibukigourd.gui.screen

import com.forpleuvoir.ibukigourd.api.Tickable
import com.forpleuvoir.ibukigourd.input.InputHandler
import com.forpleuvoir.ibukigourd.util.mc
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.BufferRenderer
import java.util.function.Consumer

object ScreenManager : Tickable {

	var current: Screen? = null
		private set

	fun setScreen(screen: Screen?) {
		current = screen
		BufferRenderer.reset()
		current?.let {
			mc.mouse.unlockCursor()
			KeyBinding.unpressAll()
			InputHandler.unpressAll()
			it.init.invoke()
			mc.skipGameRender = false
			mc.updateWindowTitle()
			return
		}
		mc.soundManager.resumeAll()
		mc.mouse.lockCursor()
		mc.updateWindowTitle()
	}

	fun open(screenScope: Screen.() -> Unit): Screen {
		val screen = object : AbstractScreen() {}.apply(screenScope)
		setScreen(screen)
		return screen
	}

	@JvmStatic
	fun hasScreen(): Boolean = current != null

	@JvmStatic
	fun hasScreen(action: Consumer<Screen>) {
		current?.let { action.accept(it) }
	}

	override fun tick() {
		current?.tick()
	}

}