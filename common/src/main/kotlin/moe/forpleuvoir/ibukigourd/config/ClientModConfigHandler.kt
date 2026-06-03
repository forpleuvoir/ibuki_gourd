package moe.forpleuvoir.ibukigourd.config

import kotlinx.coroutines.runBlocking
import moe.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.common.api.Initializable
import moe.forpleuvoir.nebula.common.util.ioLaunch


object ClientModConfigHandler : ModConfigHandler<ClientModConfigManager>, Initializable {

    private val log = logger()

    private val configManagers = LinkedHashMap<String, ClientModConfigManager>()

    override val managers: Iterable<ClientModConfigManager>
        get() = configManagers.values

    override fun register(manager: ClientModConfigManager) {
        configManagers["${manager.modId} -> ${manager.name}"] = manager
    }

    override fun init() {
        ClientLifecycleEvent.Starting.register { initManager() }
        ClientLifecycleEvent.Stopping.register { stop() }
        ClientLifecycleEvent.OpenGameMenu.register { clientSave() }
    }

    private fun initManager() {
        log.info("init client mod config")
        managers.forEach { manager ->
            manager.init()
            log.info("[${manager.modId} - ${manager.name}]client config init")
            runBlocking {
                runCatching {
                    manager.load()
                }.onFailure {
                    manager.forceSave()
                    log.error("[${manager.modId}] client config load failed", it)
                }
            }
        }
    }

    fun stop() {
        log.info("client mod config saving...")
        runBlocking { save() }
    }

    fun clientSave() {
        configManagers.forEach { (key, value) ->
            if (value.savable()) {
                log.info("[{}]auto async save client config...", key)
                val deferred = value.asyncSave()
                ioLaunch {
                    log.info("[$key]async saved client config,saving time:${deferred.await()}")
                }
            }
        }
    }

}