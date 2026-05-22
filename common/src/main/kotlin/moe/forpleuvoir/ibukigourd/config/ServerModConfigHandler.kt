package moe.forpleuvoir.ibukigourd.config

import kotlinx.coroutines.runBlocking
import moe.forpleuvoir.ibukigourd.event.events.server.ServerLifecycleEvent
import moe.forpleuvoir.ibukigourd.platform.Services
import moe.forpleuvoir.ibukigourd.util.logger
import moe.forpleuvoir.nebula.common.api.Initializable
import moe.forpleuvoir.nebula.config.startup
import net.minecraft.server.MinecraftServer

internal object ServerModConfigHandler : ModConfigHandler, Initializable {
    private val log = logger()

    private val configManagers = HashMap<String, ServerModConfigManager>()

    override val managers: Iterable<ModConfigManager>
        get() = configManagers.values


    override fun init() {
        ServerLifecycleEvent.Starting.register { initManager(it) }
        ServerLifecycleEvent.Stopping.register { serverStop() }
        ServerLifecycleEvent.Saving.register { serverSave() }
    }

    fun initManager(server: MinecraftServer) {
        log.info("init server mod config...")
        configManagers.clear()
        Services.SERVER_CONFIG_MANAGER.forEach { manager ->
            manager.init(server)
            log.info("[${manager.modId} - ${manager.name}]server config init")
            runBlocking {
                runCatching {
                    manager.load()
                }.onFailure {
                    manager.forceSave()
                    log.error("[${manager.modId}] server config load failed", it)
                }
            }
            configManagers["${manager.modId} -> ${manager.name}"] = manager
        }
    }

    fun serverStop() {
        log.info("server mod config saving...")
        runBlocking {
            save()
        }
    }

    fun serverSave() {
        configManagers.forEach { (key, value) ->
            if (value.savable()) {
                log.info("[{}]auto async save server config...", key)
                value.asyncSave().let {
                    log.info("[{}]async saved server config,saving time:$it", key)
                }
            }
        }
    }

}