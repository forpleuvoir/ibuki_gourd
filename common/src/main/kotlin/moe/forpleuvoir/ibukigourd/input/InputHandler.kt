package moe.forpleuvoir.ibukigourd.input

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import moe.forpleuvoir.ibukigourd.api.Tickable
import moe.forpleuvoir.ibukigourd.util.exactMatch
import moe.forpleuvoir.nebula.common.api.Observable
import java.util.IdentityHashMap
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

    /**
     * 记录每个 keybind 注册时建立的变更监听 disposable，注销时一并取消。
     *
     * 用 [IdentityHashMap] 以引用相等索引 keybind：[Keybind.hashCode]/`equals` 会随其内容
     * （按键、observer 列表等）变化，作为普通 map 的 key 不可靠。
     */
    private val observers: MutableMap<Keybind, Observable.Disposable> = IdentityHashMap()

    private val beforePressKeyCode: MutableList<KeyCode> = ArrayList()

    /**
     * 当前按下的所有键
     */
    private val currentPressKeyCode: MutableList<KeyCode> = ArrayList()

    fun register(keybind: Keybind): Disposable {
        require(keybinds.none { it === keybind }) { "Keybind already registered: $keybind" }
        keybinds.add(keybind)
        updateVersion()
        observers[keybind] = keybind.observe { updateVersion() }
        return {
            unregister(keybind)
        }
    }

    fun register(
        vararg keyCodes: KeyCode,
        defaultSetting: KeybindSetting = KeybindSetting(),
        action: Keybind.() -> Unit = {}
    ): Disposable {
        return register(Keybind(keyCodes = keyCodes, defaultSetting, action))
    }

    /**
     * 注销一个快捷键，并取消其在注册时建立的变更监听。
     *
     * @return 该快捷键此前已注册并被移除时返回 `true`，否则返回 `false`
     */
    fun unregister(keybind: Keybind): Boolean {
        val index = keybinds.indexOfFirst { it === keybind }
        if (index == -1) return false
        keybinds.removeAt(index)
        observers.remove(keybind)?.dispose()
        updateVersion()
        return true
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

    private fun syncBeforeKeys() {
        beforePressKeyCode.clear()
        beforePressKeyCode.addAll(currentPressKeyCode)
    }

    private inline fun dispatchKeybinds(event: (Keybind) -> Boolean): Boolean {
        var passthrough = true
        keybinds.forEach { keybind ->
            if (!event(keybind)) passthrough = false
        }
        return passthrough
    }

    @JvmStatic
    @JvmName("onKeyPress")
    fun onKeyPress(keyCode: KeyCode): Boolean {
        if (!currentPressKeyCode.contains(keyCode)) {
            currentPressKeyCode.add(keyCode)
            return try {
                dispatchKeybinds { it.onKeyPress(beforePressKeyCode, currentPressKeyCode) }
            } finally {
                syncBeforeKeys()
            }
        }
        return true
    }

    @JvmStatic
    @JvmName("onKeyRelease")
    fun onKeyRelease(keyCode: KeyCode): Boolean {
        if (currentPressKeyCode.contains(keyCode)) {
            currentPressKeyCode.remove(keyCode)
            return try {
                dispatchKeybinds { it.onKeyRelease(beforePressKeyCode, currentPressKeyCode) }
            } finally {
                syncBeforeKeys()
            }
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