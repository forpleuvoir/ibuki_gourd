package moe.forpleuvoir.ibukigourd.config

import moe.forpleuvoir.ibukigourd.util.loader
import net.fabricmc.loader.api.metadata.ModMetadata
import java.io.File
import java.nio.file.Path

abstract class ClientModConfigManager(modMetadata: ModMetadata, key: String) : ModConfigManager(modMetadata, key) {
	override val configPath: Path
		get() = File(loader.configDir.toFile(), modMetadata.id).toPath()


}