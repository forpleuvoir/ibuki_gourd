package forpleuvoir.ibuki_gourd.mod.initialize

import forpleuvoir.ibuki_gourd.common.IModInitialize
import forpleuvoir.ibuki_gourd.config.ConfigManager
import forpleuvoir.ibuki_gourd.event.EventBus
import forpleuvoir.ibuki_gourd.event.events.GameInitializedEvent
import forpleuvoir.ibuki_gourd.mod.IbukiGourdLogger
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod
import forpleuvoir.ibuki_gourd.mod.config.IbukiGourdConfigs
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
		ScreenInitialize.initialize()
		ConfigManager.register(IbukiGourdMod,IbukiGourdConfigs)
		log.info("${IbukiGourdMod.modName} Initialized...")
	}

	private fun onGameInitialized(){
		ConfigManager.loadAll()
	}
}