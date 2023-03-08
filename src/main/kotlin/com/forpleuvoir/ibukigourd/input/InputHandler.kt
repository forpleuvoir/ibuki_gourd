package com.forpleuvoir.ibukigourd.input

import com.forpleuvoir.ibukigourd.api.Tickable
import com.forpleuvoir.ibukigourd.input.KeyTriggerMode.*
import com.forpleuvoir.ibukigourd.util.NextAction
import com.forpleuvoir.ibukigourd.util.isDevEnv

object InputHandler : Tickable {

	private val keyBinds: MutableList<KeyBind> = ArrayList()

	private val beforePressKeyCode: MutableList<Int> = ArrayList()

	/**
	 * 当前按下的所有键
	 */
	private val currentPressKeyCode: MutableList<Int> = ArrayList()

	init {
		isDevEnv {
			keyBinds.add(KeyBind(KEY_LEFT_CONTROL, KEY_KP_1, defaultSetting = keyBindSetting(triggerMode = OnPress)) {
				println("按下了 1")
			})
			keyBinds.add(KeyBind(KEY_LEFT_CONTROL, KEY_KP_2, defaultSetting = keyBindSetting(triggerMode = OnPressed)) {
				println("按住了 2")
			})
			keyBinds.add(
				KeyBind(
					KEY_LEFT_CONTROL,
					KEY_KP_3,
					defaultSetting = keyBindSetting(triggerMode = OnLongPress)
				) {
					println("长按了 3")
				})
			keyBinds.add(
				KeyBind(
					KEY_LEFT_CONTROL,
					KEY_KP_4,
					defaultSetting = keyBindSetting(triggerMode = OnLongPressed)
				) {
					println("长按住了 4")
				})
			keyBinds.add(KeyBind(KEY_LEFT_CONTROL, KEY_KP_5, defaultSetting = keyBindSetting(triggerMode = OnRelease)) {
				println("释放了 5")
			})
			keyBinds.add(KeyBind(KEY_LEFT_CONTROL, KEY_KP_6, defaultSetting = keyBindSetting(triggerMode = BOTH)) {
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

	@JvmStatic
	fun onKeyPress(keyCode: Int): NextAction {
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
	fun onKeyRelease(keyCode: Int): NextAction {
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