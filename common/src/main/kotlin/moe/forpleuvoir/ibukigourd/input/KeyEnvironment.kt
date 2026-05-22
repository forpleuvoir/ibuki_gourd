package moe.forpleuvoir.ibukigourd.input

import moe.forpleuvoir.ibukigourd.util.mc
import moe.forpleuvoir.nebula.serialization.Deserializer
import moe.forpleuvoir.nebula.serialization.Serializable
import moe.forpleuvoir.nebula.serialization.base.SerializeElement
import moe.forpleuvoir.nebula.serialization.base.SerializePrimitive
import moe.forpleuvoir.nebula.serialization.codec.Codec
import moe.forpleuvoir.nebula.serialization.codec.enum

enum class KeyEnvironment(val key: String) : Serializable {
    InGame("in_game"),
    InGui("in_gui"),
    Any("any");

    companion object : Codec<KeyEnvironment> by Codec.enum<KeyEnvironment>() {

        @JvmStatic
        fun fromKey(key: String): KeyEnvironment {
            return entries.first { it.key == key }
        }
    }

    infix fun conflictOf(environment: KeyEnvironment): Boolean {
        return if (this == Any || environment == Any) true
        else this == environment
    }

    fun envMatch(): Boolean {
        if (this == Any) return true
        return this == currentEnv()
    }

    inline fun onEnvMatch(callback: () -> Unit) {
        if (envMatch()) callback.invoke()
    }

    override fun serialization(): SerializeElement {
        return SerializePrimitive(this.key)
    }

}

fun currentEnv(): KeyEnvironment {
    return if (mc.screen != null) KeyEnvironment.InGui else KeyEnvironment.InGame
}
