package forpleuvoir.ibuki_gourd.event

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import org.jetbrains.annotations.NotNull
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 事件总线

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.event

 * 文件名 EventBus

 * 创建时间 2021/12/9 9:39

 * @author forpleuvoir

 */
object EventBus {

	/**
	 * 所有事件订阅者
	 */
	private val subscribers = ConcurrentHashMap<Class<out Event>, ConcurrentHashMap<UUID, (Event) -> Unit>>()

	/**
	 * 发布一条事件
	 * @param event 事件
	 * @return Unit
	 */
	@JvmStatic
	fun <E : Event> broadcast(event: E) {
		event.broadcastHandle(getEventSubscribers())
	}

	/**
	 * 订阅事件
	 * @param subscriber Function1<Event, Unit>
	 * @return UUID
	 */
	@JvmStatic
	inline fun <reified E : Event> subscribe(noinline subscriber: (E) -> Unit): UUID {
		return subscribe(E::class.java, subscriber)
	}

	/**
	 * 订阅事件
	 * @param subscriber Function1<Event, Unit>
	 * @return UUID
	 */
	@JvmStatic
	@Suppress("UNCHECKED_CAST")
	fun <E : Event> subscribe(channel: Class<out E>, subscriber: (E) -> Unit): UUID {
		val uuid = UUID.randomUUID()
		if (subscribers.containsKey(channel)) {
			subscribers[channel]?.put(uuid, subscriber as (Event) -> Unit)
		} else {
			val map = ConcurrentHashMap<UUID, (Event) -> Unit>()
			map[uuid] = subscriber as (Event) -> Unit
			subscribers[channel] = map
		}
		return uuid
	}

	/**
	 * 退订事件
	 * @param uuid UUID
	 * @return Boolean 是否成功
	 */
	@JvmStatic
	inline fun <reified E : Event> unSubscribe(uuid: UUID): Boolean {
		return unSubscribe(E::class.java, uuid)
	}

	/**
	 * 退订事件
	 * @param channel Class 退订的事件类型
	 * @param uuid UUID
	 * @return Boolean 是否成功
	 */
	@JvmStatic
	fun <E : Event> unSubscribe(channel: Class<out E>, uuid: UUID): Boolean {
		return if (subscribers.containsKey(channel)) {
			subscribers[channel]?.remove(uuid) != null
		} else false
	}

	@JvmStatic
	fun registerSubscriber(@NotNull subscriber: Subscriber) {
		subscriber.subscribe(this)
	}

	private fun getEventSubscribers(): ImmutableMap<Class<out Event>, ImmutableList<(Event) -> Unit>> {
		val builder = ImmutableMap.Builder<Class<out Event>, ImmutableList<(Event) -> Unit>>()
		subscribers.forEach { (k: Class<out Event>, v: ConcurrentHashMap<UUID, (Event) -> Unit>) ->
			val listBuilder =
				ImmutableList.Builder<(Event) -> Unit>()
			for (consumer in v) {
				listBuilder.add(consumer.value)
			}
			builder.put(k, listBuilder.build())
		}
		return builder.build()
	}
}
