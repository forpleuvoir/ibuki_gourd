package moe.forpleuvoir.ibukigourd.config

import net.minecraft.server.MinecraftServer
import java.io.File
import java.nio.file.Path

abstract class ServerModConfigManager(
    modId: String,
    key: String,
    autoScan: AutoScan = AutoScan.close
) : ModConfigManager(modId, key, autoScan) {

    protected open lateinit var server: MinecraftServer

    fun init(server: MinecraftServer) {
        this.server = server
        init()
    }

    override val configPath: Path
        get() = File(server.storageSource.levelDirectory.directoryName(), this@ServerModConfigManager.modId).toPath()

}