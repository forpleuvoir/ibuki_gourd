package moe.forpleuvoir.ibukigourd.event.events.client.input

import moe.forpleuvoir.ibukigourd.input.KeyCode
import moe.forpleuvoir.ibukigourd.input.KeyEnvironment
import moe.forpleuvoir.ibukigourd.input.currentEnv
import moe.forpleuvoir.nebula.event.CancellableEvent

class MouseEvent {
	class MousePressEvent(
		@JvmField
		val keyCode: KeyCode,
		@JvmField
		val name: String = keyCode.keyName,
		@JvmField
		val env: KeyEnvironment = currentEnv(),
	) : CancellableEvent {
		override var canceled: Boolean = false
	}

	class MouseReleaseEvent(
		@JvmField
		val keyCode: KeyCode,
		@JvmField
		val name: String = keyCode.keyName,
		@JvmField
		val env: KeyEnvironment = currentEnv(),
	) : CancellableEvent {
		override var canceled: Boolean = false
	}

	class MouseScrollEvent(
		@JvmField
		val amount: Double,
		@JvmField
		val env: KeyEnvironment = currentEnv(),
	) : CancellableEvent {
		override var canceled: Boolean = false
	}

	class MouseMoveEvent(
		@JvmField
		val x: Double,
		@JvmField
		val y: Double,
		@JvmField
		val env: KeyEnvironment = currentEnv(),
	) : CancellableEvent {
		override var canceled: Boolean = false
	}

	class MouseDraggingEvent(
		@JvmField
		val keyCode: KeyCode,
		@JvmField
		val name: String = keyCode.keyName,
		@JvmField
		val x: Double,
		@JvmField
		val y: Double,
		@JvmField
		val env: KeyEnvironment = currentEnv(),
	) : CancellableEvent {
		override var canceled: Boolean = false
	}

}