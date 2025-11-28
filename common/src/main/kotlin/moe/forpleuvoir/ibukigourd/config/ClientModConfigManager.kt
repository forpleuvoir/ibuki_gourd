package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.platform.PLATFORM
import java.io.File
import java.nio.file.Path

abstract class ClientModConfigManager(
    modId: String,
    key: String,
    autoScan: AutoScan = AutoScan.close
) : ModConfigManager(modId, key, autoScan) {
    override val configPath: Path
        get() = File(PLATFORM.getConfigDir(), modId).toPath()

}