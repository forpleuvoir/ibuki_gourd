package moe.forpleuvoir.ibukigourd.mod.what.ecs

import moe.forpleuvoir.ibukigourd.input.KeyCode

enum class KeyAction {
    PRESS,
    RELEASE
}

fun interface InputSystem {

    fun onInput(world: World, keyCode: KeyCode, action: KeyAction, used: Boolean): Boolean

}

interface InputSystemManager {

    fun addInputSystem(system: InputSystem)

    fun removeInputSystem(system: InputSystem)

    fun onInput(world: World, keyCode: KeyCode, action: KeyAction, used: Boolean)

}

class DefaultInputSystemManager : InputSystemManager {

    private val systems = mutableListOf<InputSystem>()

    override fun addInputSystem(system: InputSystem) {
        systems.add(system)
    }

    override fun removeInputSystem(system: InputSystem) {
        systems.remove(system)
    }

    override fun onInput(world: World, keyCode: KeyCode, action: KeyAction, used: Boolean) {
        var used = used
        systems.forEach { used = it.onInput(world, keyCode, action, used) }
    }

}