package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.scanModPackage
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf

@EventSubscriber
@Deprecated("Internal objects,Do not call")
object ClientModConfigHandler : ModConfigHandler {

	private val log = logger()

	private val configManagers = HashMap<String, ClientModConfigManager>()

	override val managers: Iterable<ModConfigManager>
		get() = configManagers.values

	@Subscriber
	fun init(event: ClientLifecycleEvent.ClientStartingEvent) {
		log.info("init client mod config")
		scanModPackage { it.hasAnnotation<ModConfig>() && it.isSubclassOf(ClientModConfigManager::class) }.forEach { (modMeta, classes) ->
			classes.forEach {
				val instance = try {
					it.createInstance() as ClientModConfigManager
				} catch (_: IllegalArgumentException) {
					try {
						it.objectInstance!! as ClientModConfigManager
					} catch (e: NullPointerException) {
						throw NullPointerException("Unable to create instance of ${it.qualifiedName},Must have noArgsConstructor or be objectInstance")
					}
				}
				val annotation = it.findAnnotation<ModConfig>()!!
				instance.init()
				try {
					instance.load()
				} catch (e: Exception) {
					instance.forceSave()
					log.error(e)
				}
				configManagers["${modMeta.id} - ${annotation.name}"] = instance
			}
		}
	}

	@Subscriber
	fun stop(event: ClientLifecycleEvent.ClientStopEvent) {
		log.info("save client savable...")
		save()
	}

}