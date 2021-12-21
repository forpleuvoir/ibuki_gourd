package forpleuvoir.ibuki_gourd.mod.initialize

import forpleuvoir.ibuki_gourd.common.IModInitialize
import forpleuvoir.ibuki_gourd.common.tText
import forpleuvoir.ibuki_gourd.config.gui.ConfigScreen
import forpleuvoir.ibuki_gourd.keyboard.KeyEnvironment
import forpleuvoir.ibuki_gourd.mod.IbukiGourdLogger
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod
import forpleuvoir.ibuki_gourd.keyboard.KeyboardUtil
import forpleuvoir.ibuki_gourd.mod.config.IbukiGourdConfigs
import forpleuvoir.ibuki_gourd.mod.config.gui.IbukiGourdConfigGroup
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

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
		ScreenInitialize.initialize()
		KeyboardUtil.setOnPressCallback(InputUtil.GLFW_KEY_I, InputUtil.GLFW_KEY_G, keyEnvironment = KeyEnvironment.ALL) {
			val screen = ConfigScreen(IbukiGourdMod.modName.tText(), configGroup = IbukiGourdConfigGroup.Toggles.current)
			screen.parent = mc.currentScreen
			mc.setScreen(screen)
		}
		IbukiGourdConfigs.Values.OPEN_GUI.initKeyBind()
		log.info("${IbukiGourdMod.modName} Initialized...")
	}
}