package forpleuvoir.ibuki_gourd.mod.config

import com.google.gson.JsonObject
import forpleuvoir.ibuki_gourd.config.ConfigUtil
import forpleuvoir.ibuki_gourd.config.IConfigHandler
import forpleuvoir.ibuki_gourd.config.options.ConfigBase
import forpleuvoir.ibuki_gourd.config.options.ConfigHotkey
import forpleuvoir.ibuki_gourd.gui.screen.ScreenBase
import forpleuvoir.ibuki_gourd.keyboard.KeyBind
import forpleuvoir.ibuki_gourd.keyboard.KeyEnvironment
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod
import forpleuvoir.ibuki_gourd.mod.config.IbukiGourdConfigs.Setting.CONFIGS
import forpleuvoir.ibuki_gourd.mod.config.gui.IbukiGourdScreen
import net.minecraft.client.util.InputUtil

/**
 * mod配置

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.config

 * 文件名 IbukiGourdConfigs

 * 创建时间 2021/12/18 12:08

 * @author forpleuvoir

 */
object IbukiGourdConfigs : IConfigHandler {

	object Setting {

		@JvmStatic
		val OPEN_GUI = ConfigHotkey(
			name = "ibuki_gourd.config.open_gui",
			defaultValue = KeyBind(
				InputUtil.GLFW_KEY_I,
				InputUtil.GLFW_KEY_G,
				keyEnvironment = KeyEnvironment.IN_GAME
			) {
				IbukiGourdScreen.openScreen(ScreenBase.current)
			}
		)

		val CONFIGS: List<ConfigBase> = listOf(
			OPEN_GUI,
		)
	}

	override fun save() {
		val configFile = ConfigUtil.configFile(IbukiGourdMod)
		val json = JsonObject()
		ConfigUtil.writeConfigBase(json, "Setting", CONFIGS)
		ConfigUtil.writeJsonToFile(json, configFile)

	}

	override fun load() {
		ConfigUtil.paresJsonFile(ConfigUtil.configFile(IbukiGourdMod))?.let {
			if (it is JsonObject) {
				ConfigUtil.readConfigBase(it, "Setting", CONFIGS)
			}
		}
	}

	override fun allConfig(): List<ConfigBase> {
		return ArrayList(CONFIGS)
	}
}