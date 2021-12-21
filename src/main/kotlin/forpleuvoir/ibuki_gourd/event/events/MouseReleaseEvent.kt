package forpleuvoir.ibuki_gourd.event.events

import forpleuvoir.ibuki_gourd.event.Event
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
class MouseReleaseEvent(code: Int, @JvmField val mods: Int, @JvmField val keyEnvironment: KeyEnvironment) : Event {
	@JvmField
	val code: InputUtil.Key

	init {
		this.code = InputUtil.Type.MOUSE.createFromCode(code)
	}
}