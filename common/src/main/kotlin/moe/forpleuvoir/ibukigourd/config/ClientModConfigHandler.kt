package moe.forpleuvoir.ibukigourd.config

import kotlinx.coroutines.runBlocking
import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.ibukigourd.util.scanModPackage
import moe.forpleuvoir.nebula.event.EventSubscriber
import moe.forpleuvoir.nebula.event.Subscriber
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf

@Suppress("unused")
@EventSubscriber
internal object ClientModConfigHandler : ModConfigHandler {

    private val log = logger()

    private val configManagers = HashMap<String, ClientModConfigManager>()

    override val managers: Iterable<ModConfigManager>
        get() = configManagers.values

    @Subscriber
    fun init(event: ClientLifecycleEvent.ClientStartedEvent) {
        log.info("init client mod config")
        scanModPackage { it.hasAnnotation<ModConfig>() && it.isSubclassOf(ClientModConfigManager::class) }
            .forEach { (modId, classes) ->
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
                    log.info("[${modId} - ${annotation.name}]client config init")
                    runBlocking {
                        runCatching {
                            instance.load()
                        }.onFailure {
                            instance.forceSave()
                            log.warn(it)
                        }
                    }
                    configManagers["$modId - ${annotation.name}"] = instance
                }
            }
    }

    @Subscriber
    fun stop(event: ClientLifecycleEvent.ClientStopEvent) {
        log.info("client mod config saving...")
        runBlocking {
            save()
        }
    }

}