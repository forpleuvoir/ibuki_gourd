package forpleuvoir.ibuki_gourd.event

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap


/**
 * 事件

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.event

 * 文件名 Event

 * 创建时间 2021/12/9 11:02

 * @author forpleuvoir

 */
interface Event : IEventBroadcastHandler, Publisher {

	val type: String
		get() = this::class.java.simpleName

	/**
	 * 事件是否可以被取消
	 */
	fun cancelable(): Boolean = false

	/**
	 * 取消该事件
	 */
	fun cancel() {}

	/**
	 * 是否被取消
	 */
	val isCanceled: Boolean get() = false

	override fun broadcast() {
		EventBus.broadcast(this)
	}

	override fun broadcastHandle(subscribers: ImmutableMap<Class<out Event>, ImmutableList<(Event) -> Unit>>) {
		if (subscribers.containsKey(this.javaClass)) {
			for (listener in subscribers[this.javaClass]!!) {
				listener.invoke(this)
			}
		}
	}


}