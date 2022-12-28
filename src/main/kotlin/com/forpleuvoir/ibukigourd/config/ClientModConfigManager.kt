package com.forpleuvoir.ibukigourd.config

import com.forpleuvoir.ibukigourd.util.loader
import java.io.File
import java.nio.file.Path

abstract class ClientModConfigManager(modID: String, key: String) : ModConfigManager(modID, key) {
	override val configPath: Path
		get() = File(loader.configDir.toFile(), modID).toPath()


}