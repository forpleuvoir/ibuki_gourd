package com.forpleuvoir.ibukigourd.input

import com.forpleuvoir.ibukigourd.api.Tickable
import com.forpleuvoir.ibukigourd.util.NextAction
import net.minecraft.client.util.InputUtil

object InputHandler : Tickable {

	private val keyBinds: MutableList<KeyBind> = ArrayList()

	private val beforePressKeyCode: MutableList<Int> = ArrayList()

	/**
	 * 当前按下的所有键
	 */
	private val currentPressKeyCode: MutableList<Int> = ArrayList()

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
			println("按下")
			print("当前按键:")
			currentPressKeyCode.forEach {
				print("${InputUtil.fromKeyCode(it, 0).localizedText.string},")
			}
			println()
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
			println("释放")
			print("当前按键:")
			currentPressKeyCode.forEach {
				print("${InputUtil.fromKeyCode(it, 0).localizedText.string},")
			}
			println()
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