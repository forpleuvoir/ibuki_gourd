package forpleuvoir.ibuki_gourd.event.events

import forpleuvoir.ibuki_gourd.event.Event


/**
 * 事件

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.event.events

 * 文件名 Events

 * 创建时间 2021/12/9 13:15

 * @author forpleuvoir

 */
object Events {
	private val events = HashSet<Class<out Event>>()

	init {
		register(ClientStartTickEvent::class.java)
		register(ClientEndTickEvent::class.java)
		register(ClientStartEvent::class.java)
		register(ClientStopEvent::class.java)
		register(KeyPressEvent::class.java)
		register(KeyReleaseEvent::class.java)
		register(MousePressEvent::class.java)
		register(MouseReleaseEvent::class.java)
		register(GameInitialized::class.java)
	}

	@JvmStatic
	fun <E : Event> register(event: Class<out E>) {
		events.add(event)
	}

	fun getEvents(): Collection<Class<out Event>> {
		return events
	}

	fun getEventsName(): Collection<String> {
		return HashSet<String>().apply {
			events.forEach { add(it.simpleName) }
		}
	}

	fun getEventByName(name: String): Class<out Event>? {
		return events.find { it.simpleName == name }
	}

	fun getEventName(eventType: Class<out Event>): String {
		return eventType.simpleName
	}
}