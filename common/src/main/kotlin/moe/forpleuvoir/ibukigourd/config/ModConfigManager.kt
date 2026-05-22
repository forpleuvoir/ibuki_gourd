package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.util.ModLogger
import moe.forpleuvoir.nebula.config.ConfigManager
import moe.forpleuvoir.nebula.config.manager.component.localConfig
import moe.forpleuvoir.nebula.config.persistence.ConfigPersistence
import moe.forpleuvoir.nebula.config.persistence.yaml
import java.nio.file.Path

abstract class ModConfigManager(
    val modId: String,
    name: String,
    persistence: context(ModConfigManager)() -> ConfigPersistence
) : ConfigManager(name, LoggerExceptionHandler(ModLogger(name, modId))) {

    init {
        localConfig(configPath, persistence())
    }

    abstract val configPath: Path

}