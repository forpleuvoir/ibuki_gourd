package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.nebula.config.persistence.ConfigPersistence
import moe.forpleuvoir.nebula.config.persistence.yaml
import net.minecraft.server.MinecraftServer
import java.io.File
import java.nio.file.Path

abstract class ServerModConfigManager(
    modId: String,
    name: String,
    persistence: context(ModConfigManager)() -> ConfigPersistence = { yaml() }
) : ModConfigManager(modId, name, persistence) {

    protected open lateinit var server: MinecraftServer

    fun init(server: MinecraftServer) {
        this.server = server
        init()
    }

    override val configPath: Path get() = File(server.storageSource.levelDirectory.directoryName(), this@ServerModConfigManager.modId).toPath()

}