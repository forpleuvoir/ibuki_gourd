package com.forpleuvoir.ibukigourd.config

import com.forpleuvoir.ibukigourd.event.events.server.ServerLifecycleEvent
import com.forpleuvoir.ibukigourd.event.events.server.ServerSavingEvent
import com.forpleuvoir.ibukigourd.util.logger
import com.forpleuvoir.ibukigourd.util.scanModPackage
import com.forpleuvoir.nebula.event.EventSubscriber
import com.forpleuvoir.nebula.event.Subscriber
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
		log.info("init server mod config")
		init()
		scanModPackage { it.hasAnnotation<ModConfig>() && it.isSubclassOf(ServerModConfigManager::class) }.forEach { (modMeta, classes) ->
			classes.forEach {
				val instance = try {
					it.createInstance() as ServerModConfigManager
				} catch (_: IllegalArgumentException) {
					try {
						it.objectInstance!! as ServerModConfigManager
					} catch (e: NullPointerException) {
						throw NullPointerException("Unable to create instance of ${it.qualifiedName},Must have noArgsConstructor or be objectInstance")
					}
				}
				val annotation = it.findAnnotation<ModConfig>()!!
				instance.init(event.server)
				try {
					instance.load()
				} catch (e: Exception) {
					instance.save()
					log.error(e)
				}
				configManagers["${modMeta.id} - ${annotation.name}"] = instance
			}
		}
	}

	@Subscriber
	fun stop(event: ServerLifecycleEvent.ServerStoppingEvent) {
		log.info("save server mod config...")
		save()
	}

	@Subscriber
	fun serverSave(event: ServerSavingEvent) {
		configManagers.forEach { (key, value) ->
			if (value.needSave) {
				log.info("[{}]auto save server config...", key)
				value.saveAsync()
			}
		}
	}

}