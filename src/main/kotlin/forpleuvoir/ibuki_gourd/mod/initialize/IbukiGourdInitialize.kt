package forpleuvoir.ibuki_gourd.mod.initialize

import forpleuvoir.ibuki_gourd.api.config.IbukiGourdConfigApi
import forpleuvoir.ibuki_gourd.api.screen.IbukiGourdScreenApi
import forpleuvoir.ibuki_gourd.common.IModInitialize
import forpleuvoir.ibuki_gourd.config.ConfigManager
import forpleuvoir.ibuki_gourd.event.EventBus
import forpleuvoir.ibuki_gourd.event.events.GameInitializedEvent
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.mod.IbukiGourdLogger
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient

/**
 * Mod初始化

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.initialize

 * 文件名 IbukiGourdInitialize

 * 创建时间 2021/12/9 15:06

 * @author forpleuvoir

 */
object IbukiGourdInitialize : IModInitialize {
	private val log = IbukiGourdLogger.getLogger(this::class.java)
	private val mc = MinecraftClient.getInstance()

	override fun initialize() {
		log.info("${IbukiGourdMod.modName} initializing...")
		EventBus.subscribe<GameInitializedEvent> { onGameInitialized() }
		FabricLoader.getInstance().apply {
			getEntrypointContainers("ibScreen", IbukiGourdScreenApi::class.java).forEach {
				val metadata = it.provider.metadata
				val name = metadata.name
				try {
					val entrypoint = it.entrypoint
					ConfigManager.registerScreen(name) { entrypoint.getScreenTabFactory().invoke(ScreenBase.current) }
				} catch (e: Exception) {
					log.error(e)
				}
			}
			getEntrypointContainers("ibConfig", IbukiGourdConfigApi::class.java).forEach {
				val metadata = it.provider.metadata
				val modID = metadata.id
				try {
					val entrypoint = it.entrypoint
					ConfigManager.register(modID, entrypoint.getConfigHandlerFactory().invoke())
				} catch (e: Exception) {
					log.error(e)
				}
			}
		}
		log.info("${IbukiGourdMod.modName} Initialized...")
	}

	private fun onGameInitialized() {
		ConfigManager.loadAll()
	}
}