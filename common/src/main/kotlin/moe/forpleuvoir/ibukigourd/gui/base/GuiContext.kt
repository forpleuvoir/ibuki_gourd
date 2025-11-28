package moe.forpleuvoir.ibukigourd.gui.base

import moe.forpleuvoir.ibukigourd.gui.base.scope.GuiScope

interface GuiContext {

    val userData: MutableMap<String, Any>

}

val GuiScope<out GuiContext>.userData get() = owner().userData

@Suppress("UNCHECKED_CAST")
fun <T : Any> GuiContext.getUserDataOrDefault(key: String, defaultValue: T): T {
    return userData[key] as? T ?: defaultValue
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> GuiContext.getUserDataOrElse(key: String, defaultValue: () -> T): T {
    return userData[key] as? T ?: defaultValue()
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> GuiContext.getUserDataOrPut(key: String, defaultValue: () -> T): T {
    return userData[key] as? T ?: defaultValue().let {
        userData[key] = it
        it
    }
}


@Suppress("UNCHECKED_CAST")
fun <T : Any> GuiScope<out GuiContext>.getUserDataOrDefault(key: String, defaultValue: T): T {
    return userData[key] as? T ?: defaultValue
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> GuiScope<out GuiContext>.getUserDataOrElse(key: String, defaultValue: () -> T): T {
    return userData[key] as? T ?: defaultValue()
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> GuiScope<out GuiContext>.getUserDataOrPut(key: String, defaultValue: () -> T): T {
    return userData[key] as? T ?: defaultValue().let {
        userData[key] = it
        it
    }
}
