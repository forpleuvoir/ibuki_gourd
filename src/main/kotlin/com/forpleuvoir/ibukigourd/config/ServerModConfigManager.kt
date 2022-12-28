package com.forpleuvoir.ibukigourd.config

import net.minecraft.server.MinecraftServer
import java.io.File
import java.nio.file.Path

abstract class ServerModConfigManager(modID: String, key: String) : ModConfigManager(modID, key) {

	protected open lateinit var server: MinecraftServer

	fun init(server: MinecraftServer) {
		this.server = server
		init()
	}

	override val configPath: Path
		get() = File(server.session.directory.path.toFile(), modID).toPath()

}