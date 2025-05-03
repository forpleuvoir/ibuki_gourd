package moe.forpleuvoir.ibukigourd.input

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.util.NextAction
import moe.forpleuvoir.ibukigourd.util.exactMatch

object InputHandler : Tickable {

    private val keyBinds: MutableList<KeyBind> = ArrayList()

    private val beforePressKeyCode: MutableList<KeyCode> = ArrayList()

    /**
     * 当前按下的所有键
     */
    private val currentPressKeyCode: MutableList<KeyCode> = ArrayList()

    fun register(keyBind: KeyBind): KeyBind {
        keyBinds.add(keyBind)
        return keyBind
    }

    fun register(
        vararg keyCodes: KeyCode,
        defaultSetting: KeyBindSetting = KeyBindSetting(),
        action: KeyBind.() -> Unit = {}
    ): KeyBind {
        return register(KeyBind(keyCodes = keyCodes, defaultSetting, action))
    }

    fun detectKeyConflicts(keyBind: KeyBind): Sequence<KeyBind> {
        if (keyBind.keys.isEmpty()) return emptySequence()
        return keyBinds.asSequence().filter {
            it !== keyBind
        }.filter {
            it.keys.exactMatch(keyBind.keys)
        }
    }

    fun unregister(keyBind: KeyBind) {
        keyBinds.remove(keyBind)
    }

    override fun onTick() {
        keyBinds.forEach {
            it.onTick()
        }
    }

    fun releaseAll() {
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
                if (action == NextAction.Cancel) return@loop
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
                if (action == NextAction.Cancel) return@loop
            }
            beforePressKeyCode.clear()
            beforePressKeyCode.addAll(currentPressKeyCode)
            return action
        }
        return NextAction.Continue
    }

    fun wasKeyPressed(keyCode: KeyCode): Boolean {
        return currentPressKeyCode.contains(keyCode)
    }

    fun wasKeyPressed(vararg keyCode: KeyCode): Boolean {
        return currentPressKeyCode.exactMatch(keyCode.toList())
    }

}