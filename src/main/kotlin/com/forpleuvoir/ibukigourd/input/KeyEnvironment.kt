package com.forpleuvoir.ibukigourd.input

import com.forpleuvoir.ibukigourd.util.mc
import com.forpleuvoir.ibukigourd.util.text.Text
import com.forpleuvoir.ibukigourd.util.text.translatable

enum class KeyEnvironment(val key: String) {
	InGame("in_game"),
	InScreen("in_screen"),
	Both("both");

	val displayName: Text
		get() = translatable("ibuki_gourd.key_bind.environment.${key}")

	val description: Text
		get() = translatable("ibuki_gourd.key_bind.environment.${key}.description")

	fun envMatch(): Boolean {
		if (this == Both) return true
		return this == currentEnv()
	}

	inline fun onEnvMatch(callback: () -> Unit) {
		if (envMatch()) callback.invoke()
	}

	fun fromKey(key: String): KeyEnvironment {
		allOption.forEach {
			if (it.key == key) return it
		}
		return InGame
	}

	val allOption: List<KeyEnvironment>
		get() = values().toList()

}

fun currentEnv(): KeyEnvironment {
	return if (mc.currentScreen == null) KeyEnvironment.InGame else KeyEnvironment.InScreen
}
