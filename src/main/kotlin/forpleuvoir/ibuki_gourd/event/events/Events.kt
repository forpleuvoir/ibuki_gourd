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
	private val events = HashMap<String, Class<out Event>>()

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
		val simpleName = event.simpleName
		events[simpleName] = event
	}

	fun getEvents(): Iterable<Class<out Event>> {
		return events.values
	}

	fun getEventsName(): Iterable<String> {
		return events.keys
	}

	fun getEventByName(name: String): Class<out Event>? {
		return events[name]
	}

	fun getEventName(eventType: Class<out Event>): String {
		var name = ""
		for ((key, value) in events) {
			if (value == eventType) {
				name = key
				break
			}
		}
		return name
	}
}