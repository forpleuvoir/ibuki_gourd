package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.platform.PLATFORM
import moe.forpleuvoir.nebula.config.persistence.ConfigPersistence
import moe.forpleuvoir.nebula.config.persistence.yaml
import java.io.File
import java.nio.file.Path

abstract class ClientModConfigManager(
    modId: String,
    name: String,
    persistence: context(ModConfigManager)() -> ConfigPersistence = { yaml() }
) : ModConfigManager(modId, name, persistence) {
    override val configPath: Path get() = File(PLATFORM.getConfigDir(), modId).toPath()
}