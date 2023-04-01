package com.forpleuvoir.ibukigourd.event.events.client.input

import com.forpleuvoir.ibukigourd.input.KeyCode
import com.forpleuvoir.ibukigourd.input.KeyEnvironment
import com.forpleuvoir.ibukigourd.input.currentEnv
import com.forpleuvoir.nebula.event.CancellableEvent
import net.minecraft.client.util.InputUtil

class KeyboardEvent {

	class KeyPressEvent(
		@JvmField
		val keyCode: KeyCode,
		@JvmField
		val name: String = InputUtil.fromKeyCode(keyCode.code, 0).localizedText.string,
		@JvmField
		val env: KeyEnvironment = currentEnv()
	) : CancellableEvent {
		override var canceled: Boolean = false
	}


	class KeyReleaseEvent(
		@JvmField
		val keyCode: KeyCode,
		@JvmField
		val name: String = InputUtil.fromKeyCode(keyCode.code, 0).localizedText.string,
		@JvmField
		val env: KeyEnvironment = currentEnv()
	) : CancellableEvent {
		override var canceled: Boolean = false
	}
}