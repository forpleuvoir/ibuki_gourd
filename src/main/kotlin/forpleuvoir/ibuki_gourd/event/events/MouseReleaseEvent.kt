package forpleuvoir.ibuki_gourd.event.events

import forpleuvoir.ibuki_gourd.event.CancelableEvent
import forpleuvoir.ibuki_gourd.keyboard.KeyEnvironment
import net.minecraft.client.util.InputUtil


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.event.events

 * 文件名 MouseReleaseEvent

 * 创建时间 2021/12/21 20:32

 * @author forpleuvoir

 */
class MouseReleaseEvent(
	@JvmField val mouseCode: Int,
	@JvmField val mods: Int,
	@JvmField val keyEnvironment: KeyEnvironment
) : CancelableEvent() {

	@JvmField
	val code: InputUtil.Key = InputUtil.Type.MOUSE.createFromCode(mouseCode)

}