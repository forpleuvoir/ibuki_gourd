package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.event.events.server.ServerLifecycleEvent
import moe.forpleuvoir.ibukigourd.event.events.server.ServerSavingEvent
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.scanModPackage
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber
import java.util.*
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf

@EventSubscriber
@Deprecated("Internal objects,Do not call")
object ServerModConfigHandler : ModConfigHandler {
	private val log = logger()

	private val configManagers = HashMap<String, ServerModConfigManager>()

	override val managers: Iterable<ModConfigManager>
		get() = configManagers.values

	private fun init() {
		Timer().schedule(object : TimerTask() {
			override fun run() {
				configManagers.forEach { (key, value) ->
					if (value.needSave) {
						log.info("[{}]auto save server config...", key)
						value.saveAsync()
					}
				}
			}
		}, 0, 1000 * 30)
	}


	@Subscriber
	fun init(event: ServerLifecycleEvent.ServerStartingEvent) {
		log.info("init server mod config...")
		init()
		scanModPackage { it.hasAnnotation<ModConfig>() && it.isSubclassOf(ServerModConfigManager::class) }.forEach { (modMeta, classes) ->
			classes.forEach { kClass ->

				val instance = runCatching {
					// 尝试创建实例
					kClass.createInstance() as ServerModConfigManager
				}.recoverCatching {
					// 如果创建实例失败，尝试获取 objectInstance
					kClass.objectInstance as ServerModConfigManager
				}.getOrElse {
					// 如果两者都失败，抛出异常
					throw Exception("Unable to create instance of ${kClass.qualifiedName}, must have noArgsConstructor or be objectInstance")
				}

				val annotation = kClass.findAnnotation<ModConfig>()!!
				instance.init(event.server)
				log.info("[${modMeta.id} - ${annotation.name}]server config init")
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
	@Suppress("unused")
	fun stop(event: ServerLifecycleEvent.ServerStoppingEvent) {
		log.info("save server mod config...")
		save()
	}

	@Subscriber
	@Suppress("unused")
	fun serverSave(event: ServerSavingEvent) {
		configManagers.forEach { (key, value) ->
			if (value.needSave) {
				log.info("[{}]auto async save server config...", key)
				value.saveAsync()
			}
		}
	}

}