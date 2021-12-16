package forpleuvoir.ibuki_gourd.event

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap


/**
 * 事件发布处理器

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.event

 * 文件名 IEventBroadcastHandler

 * 创建时间 2021/12/9 11:07

 * @author forpleuvoir

 */
interface IEventBroadcastHandler {
	fun broadcastHandle(
		subscribers: ImmutableMap<Class<out Event>, ImmutableList<(Event) -> Unit>>
	)
}