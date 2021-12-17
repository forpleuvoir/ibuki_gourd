package forpleuvoir.ibuki_gourd.mod.initialize

import forpleuvoir.ibuki_gourd.common.IModInitialize
import forpleuvoir.ibuki_gourd.event.events.KeyPressEvent
import forpleuvoir.ibuki_gourd.event.events.KeyReleaseEvent
import forpleuvoir.ibuki_gourd.keyboard.KeyboardUtil
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.initialize

 * 文件名 ScreenInitialize

 * 创建时间 2021/12/15 21:56

 * @author forpleuvoir

 */
object ScreenInitialize : IModInitialize {

	override fun initialize() {
		ScreenEvents.BEFORE_INIT.register(this::beforeInit)
		ScreenEvents.AFTER_INIT.register(this::afterInit)
	}

	private fun beforeInit(client: MinecraftClient, screen: Screen, width: Int, height: Int) {

	}

	private fun afterInit(client: MinecraftClient, screen: Screen, width: Int, height: Int) {
//		ScreenKeyboardEvents.afterKeyPress(screen).register { _, key, scancode, modifiers ->
//			KeyboardUtil.setPressed(key)
//			KeyPressEvent(key, scancode, modifiers).broadcast()
//		}
//		ScreenKeyboardEvents.afterKeyRelease(screen).register { _, key, scancode, modifiers ->
//			KeyboardUtil.setRelease(key)
//			KeyReleaseEvent(key, scancode, modifiers).broadcast()
//		}
	}
}