package forpleuvoir.ibuki_gourd.event.events

import forpleuvoir.ibuki_gourd.event.CancelableEvent
import forpleuvoir.ibuki_gourd.keyboard.KeyEnvironment
import net.minecraft.client.util.InputUtil


/**
 * 按键释放事件

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.event.events

 * 文件名 KeyReleaseEvent

 * 创建时间 2021/12/15 22:25

 * @author forpleuvoir

 */
class KeyReleaseEvent(
	@JvmField val keyCode: Int,
	scancode: Int,
	@JvmField val modifiers: Int,
	@JvmField val environment: KeyEnvironment
) : CancelableEvent() {

	@JvmField
	val key: InputUtil.Key

	init {
		this.key = InputUtil.fromKeyCode(keyCode, scancode)
	}
}