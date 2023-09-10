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
		scanModPackage { it.hasAnnotation<ModConfig>() && it.isSubclassOf(ClientModConfigManager::class) }
			.forEach { (modMeta, classes) ->
				classes.forEach { kClass ->

					val instance = runCatching {
						// 尝试创建实例
						kClass.createInstance() as ClientModConfigManager
					}.recoverCatching {
						// 如果创建实例失败，尝试获取 objectInstance
						kClass.objectInstance as ClientModConfigManager
					}.getOrElse {
						// 如果两者都失败，抛出异常
						throw Exception("Unable to create instance of ${kClass.qualifiedName}, must have noArgsConstructor or be objectInstance")
					}

					val annotation = kClass.findAnnotation<ModConfig>()!!
					instance.init()
					log.info("[${modMeta.id} - ${annotation.name}]client config init")
					runCatching {
						instance.load()
					}.onFailure {
						instance.forceSave()
						log.error(it)
					}
					configManagers["${modMeta.id} - ${annotation.name}"] = instance
				}
			}
	}

	@Subscriber
	fun stop(event: ClientLifecycleEvent.ClientStopEvent) {
		log.info("client mod config saving...")
		save()
	}

}