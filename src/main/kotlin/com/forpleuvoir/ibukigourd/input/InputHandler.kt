package com.forpleuvoir.ibukigourd.input

import com.forpleuvoir.ibukigourd.api.Tickable
import com.forpleuvoir.ibukigourd.gui.screen.testScreen
import com.forpleuvoir.ibukigourd.input.KeyTriggerMode.*
import com.forpleuvoir.ibukigourd.input.Keyboard.*
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.ibukigourd.util.isDevEnv

object InputHandler : Tickable {

	private val keyBinds: MutableList<KeyBind> = ArrayList()

	private val beforePressKeyCode: MutableList<KeyCode> = ArrayList()

	/**
	 * 当前按下的所有键
	 */
	private val currentPressKeyCode: MutableList<KeyCode> = ArrayList()

	init {
		isDevEnv {
			register(KeyBind(KP_1, defaultSetting = keyBindSetting(triggerMode = OnPress)) {
				testScreen()
				println("打开测试屏幕")
			})
			register(KeyBind(LEFT_CONTROL, KP_1, defaultSetting = keyBindSetting(triggerMode = OnPress)) {
				println("按下了 1")
			})
			register(KeyBind(LEFT_CONTROL, KP_2, defaultSetting = keyBindSetting(triggerMode = OnPressed)) {
				println("按住了 2")
			})
			register(
				KeyBind(LEFT_CONTROL, KP_3, defaultSetting = keyBindSetting(triggerMode = OnLongPress)) {
					println("长按了 3")
				})
			register(
				KeyBind(LEFT_CONTROL, KP_4, defaultSetting = keyBindSetting(triggerMode = OnLongPressed)) {
					println("长按住了 4")
				})
			register(KeyBind(LEFT_CONTROL, KP_5, defaultSetting = keyBindSetting(triggerMode = OnRelease)) {
				println("释放了 5")
			})
			register(KeyBind(LEFT_CONTROL, KP_6, defaultSetting = keyBindSetting(triggerMode = BOTH)) {
				println("按下或者释放了 6")
			})
		}
	}

	fun register(keyBind: KeyBind) {
		keyBinds.add(keyBind)
	}

	fun unregister(keyBind: KeyBind) {
		keyBinds.remove(keyBind)
	}

	override fun tick() {
		keyBinds.forEach {
			it.tick()
		}
	}

	fun unpressAll() {
		keyBinds.forEach(KeyBind::rest)
		currentPressKeyCode.clear()
		beforePressKeyCode.clear()
	}

	@JvmStatic
	fun onKeyPress(keyCode: KeyCode): NextAction {
		if (!currentPressKeyCode.contains(keyCode)) {
			//changed
			currentPressKeyCode.add(keyCode)
			var action = NextAction.Continue
			keyBinds.forEach loop@{
				action = it.onKeyPress(beforePressKeyCode, currentPressKeyCode)
				if (action == NextAction.Continue) return@loop
			}
			beforePressKeyCode.clear()
			beforePressKeyCode.addAll(currentPressKeyCode)
			return action
		}
		return NextAction.Continue
	}

	@JvmStatic
	fun onKeyRelease(keyCode: KeyCode): NextAction {
		if (currentPressKeyCode.contains(keyCode)) {
			//changed
			currentPressKeyCode.remove(keyCode)
			var action = NextAction.Continue
			keyBinds.forEach loop@{
				action = it.onKeyRelease(beforePressKeyCode, currentPressKeyCode)
				if (action == NextAction.Continue) return@loop
			}
			beforePressKeyCode.clear()
			beforePressKeyCode.addAll(currentPressKeyCode)
			return action
		}
		return NextAction.Continue
	}


}