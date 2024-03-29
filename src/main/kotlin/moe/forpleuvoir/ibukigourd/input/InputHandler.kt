package moe.forpleuvoir.ibukigourd.input

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.gui.screen.testScreen
import moe.forpleuvoir.ibukigourd.input.Keyboard.KP_1
import moe.forpleuvoir.ibukigourd.input.Keyboard.KP_2
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.exactMatch
import moe.forpleuvoir.ibukigourd.util.isDevEnv

object InputHandler : Tickable {

    private val keyBinds: MutableList<KeyBind> = ArrayList()

    private val beforePressKeyCode: MutableList<KeyCode> = ArrayList()

    /**
     * 当前按下的所有键
     */
    private val currentPressKeyCode: MutableList<KeyCode> = ArrayList()

    init {
        isDevEnv {
            register(KP_1) {
                testScreen(1)
            }
            register(KP_2) {
                testScreen(2)
            }
            register(Keyboard.KP_3) {
                testScreen(3)
            }
        }
    }

    fun register(keyBind: KeyBind): KeyBind {
        keyBinds.add(keyBind)
        return keyBind
    }

    fun register(
        vararg keyCodes: KeyCode,
        defaultSetting: KeyBindSetting = keyBindSetting(),
        action: KeyBind.() -> Unit = {}
    ): KeyBind {
        return register(KeyBind(keyCodes = keyCodes, defaultSetting, action))
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
    @JvmName("onKeyPress")
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
    @JvmName("onKeyRelease")
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

    fun hasKeyPressed(keyCode: KeyCode): Boolean {
        return currentPressKeyCode.contains(keyCode)
    }

    fun hasKeyPressed(vararg keyCode: KeyCode): Boolean {
        return currentPressKeyCode.exactMatch(keyCode.toList())
    }

}