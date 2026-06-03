package moe.forpleuvoir.ibukigourd.input

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.util.exactMatch
import java.util.concurrent.CopyOnWriteArrayList

object InputHandler : Tickable {

    fun interface Disposable {
        fun dispose()
    }

    var keybindVersion: Long by mutableStateOf(0L)

    private fun updateVersion() {
        if (keybindVersion <= 1145141919810)
            keybindVersion++
        else keybindVersion = 0
    }

    private val keybinds: MutableList<Keybind> = CopyOnWriteArrayList()

    private val beforePressKeyCode: MutableList<KeyCode> = ArrayList()

    /**
     * 当前按下的所有键
     */
    private val currentPressKeyCode: MutableList<KeyCode> = ArrayList()

    fun register(keybind: Keybind): Disposable {
        keybinds.add(keybind)
        updateVersion()
        val obsDisposable = keybind.observe { updateVersion() }
        return {
            obsDisposable.dispose()
            keybinds.remove(keybind)
            updateVersion()
        }
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