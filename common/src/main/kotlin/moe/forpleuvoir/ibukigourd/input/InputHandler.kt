package moe.forpleuvoir.ibukigourd.input

import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.util.exactMatch

object InputHandler : Tickable {

    fun interface Disposable {
        fun dispose()
    }

    private val keybinds: MutableList<Keybind> = ArrayList()

    private val beforePressKeyCode: MutableList<KeyCode> = ArrayList()

    /**
     * 当前按下的所有键
     */
    private val currentPressKeyCode: MutableList<KeyCode> = ArrayList()

    fun register(keyBind: Keybind): Disposable {
        keybinds.add(keyBind)
        return { keybinds.remove(keyBind) }
    }

    fun register(
        vararg keyCodes: KeyCode,
        defaultSetting: KeybindSetting = KeybindSetting(),
        action: Keybind.() -> Unit = {}
    ): Disposable {
        return register(Keybind(keyCodes = keyCodes, defaultSetting, action))
    }

    fun detectKeyConflicts(keyBind: Keybind): Sequence<Keybind> {
        if (keyBind.keys.isEmpty()) return emptySequence()
        return keybinds.asSequence().filter {
            it !== keyBind
        }.filter {
            it.setting.env conflictOf keyBind.setting.env && it.keys.exactMatch(keyBind.keys)
        }
    }

    override fun onTick() {
        keybinds.forEach(Keybind::onTick)
    }

    fun releaseAll() {
        keybinds.forEach(Keybind::resetState)
        currentPressKeyCode.clear()
        beforePressKeyCode.clear()
    }

    @JvmStatic
    @JvmName("onKeyPress")
    fun onKeyPress(keyCode: KeyCode): Boolean {
        if (!currentPressKeyCode.contains(keyCode)) {
            //changed
            currentPressKeyCode.add(keyCode)
            var action = true
            keybinds.forEach loop@{
                action = it.onKeyPress(beforePressKeyCode, currentPressKeyCode)
                if (!action) return@loop
            }
            beforePressKeyCode.clear()
            beforePressKeyCode.addAll(currentPressKeyCode)
            return action
        }
        return true
    }

    @JvmStatic
    @JvmName("onKeyRelease")
    fun onKeyRelease(keyCode: KeyCode): Boolean {
        if (currentPressKeyCode.contains(keyCode)) {
            //changed
            currentPressKeyCode.remove(keyCode)
            var action = true
            keybinds.forEach loop@{
                action = it.onKeyRelease(beforePressKeyCode, currentPressKeyCode)
                if (!action) return@loop
            }
            beforePressKeyCode.clear()
            beforePressKeyCode.addAll(currentPressKeyCode)
            return action
        }
        return true
    }

    val pressedKeys: Set<KeyCode> get() = currentPressKeyCode.toSet()

    fun wasKeyPressed(keyCode: KeyCode): Boolean {
        return currentPressKeyCode.contains(keyCode)
    }

    fun wasKeyPressed(vararg keyCode: KeyCode): Boolean {
        return currentPressKeyCode.exactMatch(keyCode.toList())
    }

}