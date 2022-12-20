package forpleuvoir.ibuki_gourd.event.events

import forpleuvoir.ibuki_gourd.common.ModInfo
import forpleuvoir.ibuki_gourd.common.mText
import forpleuvoir.ibuki_gourd.common.tText
import forpleuvoir.ibuki_gourd.event.Event
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod
import net.minecraft.text.Text
import net.minecraft.text.TranslatableTextContent


/**
 * 事件

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.event.events

 * 文件名 Events

 * 创建时间 2021/12/9 13:15

 * @author forpleuvoir

 */
object Events {
	private val events = LinkedHashSet<Class<out Event>>()
	private val descriptions = HashMap<Class<out Event>, Text>()

	init {
		register(ClientStartTickEvent::class.java)
		register(ClientEndTickEvent::class.java)
		register(ClientStartEvent::class.java)
		register(ClientStopEvent::class.java)
		register(KeyPressEvent::class.java)
		register(KeyReleaseEvent::class.java)
		register(MousePressEvent::class.java)
		register(MouseReleaseEvent::class.java)
		register(GameInitializedEvent::class.java)
	}

	@JvmStatic
	fun <E : Event> register(event: Class<out E>, modInfo: ModInfo) {
		events.add(event)
		descriptions[event] = translate(modInfo, event).mText
	}

	@JvmStatic
	fun <E : Event> register(event: Class<out E>, description: Text) {
		events.add(event)
		descriptions[event] = description
	}

	private fun <E : Event> register(event: Class<out E>) {
		events.add(event)
		descriptions[event] = translate(event).mText
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

	fun getDescription(eventType: Class<out Event>): Text? {
		return descriptions[eventType]
	}

	fun getDescription(eventType: String): Text? {
		return descriptions[getEventByName(eventType)]
	}

	fun getEventName(eventType: Class<out Event>): String {
		return eventType.simpleName
	}

	private fun translate(key: Class<out Event>): TranslatableTextContent {
		return "${IbukiGourdMod.modId}.event.description.${key.simpleName}".tText()
	}

	fun translate(modInfo: ModInfo, key: Class<out Event>): TranslatableTextContent {
		return "${modInfo.modId}.event.description.${key.simpleName}".tText()
	}
}