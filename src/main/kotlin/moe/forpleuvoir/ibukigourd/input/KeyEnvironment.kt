package moe.forpleuvoir.ibukigourd.input

import moe.forpleuvoir.ibukigourd.gui.screen.ScreenManager
import moe.forpleuvoir.ibukigourd.text.Text
import moe.forpleuvoir.ibukigourd.text.Translatable
import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive

enum class KeyEnvironment(val key: String) : Serializable {
    InGame("in_game"),
    InScreen("in_screen"),
    Both("both");

    companion object : Deserializer<KeyEnvironment> {

        override fun deserialization(serializeElement: SerializeElement): KeyEnvironment {
            return KeyEnvironment.fromKey(serializeElement.asString)
        }

        @JvmStatic
        fun fromKey(key: String): KeyEnvironment {
            return entries.first { it.key == key }
        }
    }

    val displayName: Text
        get() = Translatable("ibuki_gourd.key_bind.environment.${key}")

    val description: Text
        get() = Translatable("ibuki_gourd.key_bind.environment.${key}.description")

    fun envMatch(): Boolean {
        if (this == Both) return true
        return this == currentEnv()
    }

    inline fun onEnvMatch(callback: () -> Unit) {
        if (envMatch()) callback.invoke()
    }

    override fun serialization(): SerializeElement {
        return SerializePrimitive(this.key)
    }

    val allOption: List<KeyEnvironment>
        get() = entries

}

fun currentEnv(): KeyEnvironment {
    return if (mc.currentScreen != null || ScreenManager.hasScreen()) KeyEnvironment.InScreen else KeyEnvironment.InGame
}
